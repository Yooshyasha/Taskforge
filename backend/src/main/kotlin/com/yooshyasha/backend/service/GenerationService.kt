package com.yooshyasha.backend.service

import com.yooshyasha.backend.dto.controller.RequestConfirmTasks
import com.yooshyasha.backend.dto.controller.RequestStartGenerate
import com.yooshyasha.backend.dto.controller.ResponseConfirm
import com.yooshyasha.backend.dto.entity.InProcessDTO
import com.yooshyasha.backend.exceptions.GeneratedTasksNotFound
import com.yooshyasha.backend.feign.AiServiceFeignClient
import com.yooshyasha.backend.storage.GeneratedTasksStorage
import com.yooshyasha.backend.storage.InProcessStorage
import dto.GenerateRequest
import dto.GeneratedTasksResponse
import dto.ResponseGetTaskStatus
import dto.ResponsePostGenerate
import dto.project.VikunjaTaskDTO
import enum.TaskStatus
import exceptions.ApiException
import exceptions.TaskNotFound
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class GenerationService(
    private val aiServiceFeignClient: AiServiceFeignClient,
    private val generatedTasksStorage: GeneratedTasksStorage,
    private val inProcessStorage: InProcessStorage,
    private val vikunjaService: VikunjaService,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)

    fun generate(data: RequestStartGenerate): ResponsePostGenerate {
        var projectTasks: List<VikunjaTaskDTO> = listOf()
        val generateData = if (data.projectId == null) {
            GenerateRequest(text = data.text, vikunjaProject = null)
        } else {
            val project = vikunjaService.getProject(data.projectId)
            projectTasks = project.tasks
            GenerateRequest(text = data.text, vikunjaProject = project)
        }

        val response = try {
            aiServiceFeignClient.generate(generateData)
        } catch (e: feign.FeignException.BadRequest) {
            throw ApiException("Invalid request", 400)
        } catch (e: feign.FeignException) {
            throw ApiException("Service error: ${e.message}", e.status())
        } catch (e: Exception) {
            throw ApiException("Unexpected error", 500)
        }

        inProcessStorage.save(response.taskId, InProcessDTO(data.projectId, projectTasks))

        return response
    }

    fun getTask(taskId: UUID): ResponseGetTaskStatus {
        return try {
            return try {
                val result = generatedTasksStorage.getTasks(taskId)
                ResponseGetTaskStatus(TaskStatus.COMPLETE, result)
            } catch (e: GeneratedTasksNotFound) {
                aiServiceFeignClient.getTask(taskId).let { response ->
                    if (response.status == TaskStatus.COMPLETE) {
                        generatedTasksStorage.save(
                            taskId,
                            GeneratedTasksResponse(
                                response.generatedTasks!!.tasks,
                                response.generatedTasks!!.projectName
                            )
                        )
                    }
                    return response
                }
            }
        } catch (e: feign.FeignException.NotFound) {
            throw TaskNotFound()
        } catch (e: feign.FeignException) {
            ResponseGetTaskStatus(TaskStatus.FAILED, null)
        } catch (e: Exception) {
            throw ApiException("Unexpected error", 500)
        }
    }

    fun confirm(taskId: UUID, data: RequestConfirmTasks): ResponseConfirm {
        generatedTasksStorage.update(taskId, data)
        val creationData = generatedTasksStorage.getTasks(taskId)

        var success = true

        val project = vikunjaService.createProject(creationData.projectName)
        creationData.tasks.onEach { task ->
            try {
                val apiTask = vikunjaService.createTask(project.id, task.name, task.description)
                task.comments?.onEach { comment ->
                    try {
                        vikunjaService.addCommentToTask(apiTask.id, comment)
                    } catch (e: Exception) {
                        logger.error("Error send comment ($comment) to task (${apiTask.id})", e)
                        success = false
                    }
                }
                task.tags.onEach { tag ->
                    try {
                        val label = vikunjaService.createLabel(tag)
                        vikunjaService.addLabelToTask(apiTask.id, label.id)
                    } catch (e: Exception) {
                        logger.error("Error add label ($tag) to task (${apiTask.id})", e)
                        success = false
                    }
                }
            } catch (e: Exception) {
                logger.error("Error processing send task ($task)", e)
                success = false
            }
        }

        generatedTasksStorage.remove(taskId)
        return ResponseConfirm(success, creationData)
    }
}
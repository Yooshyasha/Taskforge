package com.yooshyasha.backend.service

import com.yooshyasha.backend.dto.api.ProjectResponse
import com.yooshyasha.backend.dto.controller.RequestConfirmTasks
import com.yooshyasha.backend.dto.controller.RequestStartGenerate
import com.yooshyasha.backend.dto.controller.ResponseConfirm
import com.yooshyasha.backend.dto.controller.ResponseGenerate
import com.yooshyasha.backend.dto.entity.InProcessDTO
import com.yooshyasha.backend.exceptions.GeneratedTasksNotFound
import com.yooshyasha.backend.exceptions.InvalidStateForSendAnswer
import com.yooshyasha.backend.feign.AiServiceFeignClient
import com.yooshyasha.backend.storage.GeneratedTasksStorage
import com.yooshyasha.backend.storage.InProcessStorage
import dto.*
import dto.project.VikunjaTaskDTO
import enum.TaskControl
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
        } catch (_: feign.FeignException.BadRequest) {
            throw ApiException("Invalid request", 400)
        } catch (e: feign.FeignException) {
            throw ApiException("Service error: ${e.message}", e.status())
        } catch (_: Exception) {
            throw ApiException("Unexpected error", 500)
        }

        inProcessStorage.save(response.taskId, InProcessDTO(data.projectId, projectTasks))

        return response
    }

    fun getTask(taskId: UUID): ResponseGenerate {
        return try {
            val result = generatedTasksStorage.getTasks(taskId)
            val inProcessDTO = inProcessStorage.get(taskId) ?: throw TaskNotFound()

            ResponseGenerate(
                ResponseGetTaskStatus(TaskStatus.COMPLETE, result),
                inProcessDTO.tasks,
                inProcessDTO.projectId,
            )
        } catch (_: feign.FeignException.NotFound) {
            throw TaskNotFound()
        } catch (_: feign.FeignException) {
            val response = ResponseGetTaskStatus(TaskStatus.FAILED, null)
            ResponseGenerate(response, null, null)
        } catch (_: GeneratedTasksNotFound) {
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
                val inProcessDTO = inProcessStorage.get(taskId) ?: throw TaskNotFound()

                ResponseGenerate(response, inProcessDTO.tasks, inProcessDTO.projectId)
            }
        } catch (_: Exception) {
            throw ApiException("Unexpected error", 500)
        }
    }

    fun confirm(taskId: UUID, data: RequestConfirmTasks): ResponseConfirm {
        generatedTasksStorage.update(taskId, data)
        val creationData = generatedTasksStorage.getTasks(taskId)

        var success = true

        val inProcessDTO = inProcessStorage.get(taskId) ?: throw TaskNotFound()
        val project = if (inProcessDTO.projectId == null) {
            vikunjaService.createProject(creationData.projectName)
        } else {
            val vikunjaProject = vikunjaService.getProject(inProcessDTO.projectId)
            ProjectResponse(vikunjaProject.id, vikunjaProject.name)
        }
        creationData.tasks.onEach { task ->
            when (task.control) {
                TaskControl.DELETE -> {
                    try {
                        vikunjaService.deleteTask(task.vikunjaTaskId!!)
                    } catch (e: Exception) {
                        logger.error("Error delete task (${task.vikunjaTaskId})", e)
                        success = false
                    }
                }

                TaskControl.EDIT -> {
                    try {
                        vikunjaService.updateTask(task.vikunjaTaskId!!, task)
                    } catch (e: Exception) {
                        logger.error("Error edit task (${task.vikunjaTaskId})", e)
                    }
                }

                TaskControl.CREATE -> {
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
            }
        }

        generatedTasksStorage.remove(taskId)
        inProcessStorage.delete(taskId)
        return ResponseConfirm(success, creationData)
    }

    fun sendAnswer(data: RequestSendAnswer): ResponseGetTaskStatus {
        val generate = getTask(data.taskId)

        if (generate.status != TaskStatus.QUESTION) throw InvalidStateForSendAnswer()

        return aiServiceFeignClient.sendAnswer(data)
    }
}
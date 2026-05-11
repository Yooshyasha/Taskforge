package com.yooshyasha.aiservice.service

import com.yooshyasha.aiservice.storage.AIQuestionStorage
import com.yooshyasha.aiservice.storage.FutureStatusStorage
import com.yooshyasha.aiservice.storage.FutureStorage
import dto.GeneratedTasksResponse
import dto.ResponseGetTaskStatus
import dto.ResponsePostGenerate
import dto.project.VikunjaProjectDTO
import enum.TaskStatus
import kotlinx.coroutines.Deferred
import org.springframework.stereotype.Service
import java.util.*

@Service
class GenerationService(
    private val aiTaskGenerationService: AITaskGenerationService,
    private val futureStorage: FutureStorage,
    private val futureStatusStorage: FutureStatusStorage,
    private val aIQuestionStorage: AIQuestionStorage,
) {
    private fun vikunjaTasksToString(vikunjaProject: VikunjaProjectDTO): String {
        var result = "\n\nEDIT PROJECT. TASKS:"

        vikunjaProject.tasks.onEach { task ->
            result += "\n${task.id}. ${task.name} (${task.status}): ${task.description}"
        }

        return result
    }

    fun generate(text: String, vikunjaProject: VikunjaProjectDTO?): ResponsePostGenerate {
        var llmRequest = text
        vikunjaProject?.let { llmRequest += vikunjaTasksToString(vikunjaProject) }

        val taskId = UUID.randomUUID()
        val task = aiTaskGenerationService.generation(
            llmRequest,
            isEdit = vikunjaProject != null,
        )
        futureStorage.save(taskId, task)

        return ResponsePostGenerate(taskId)
    }

    suspend fun getTask(taskId: UUID): ResponseGetTaskStatus {
        val task: Deferred<GeneratedTasksResponse> = futureStorage.getTask(taskId)
        val futureStatus = futureStatusStorage.getStatus(taskId)

        val response: GeneratedTasksResponse?
        try {
            response = aiTaskGenerationService.getTaskResult(task)
        } catch (e: Exception) {
            futureStorage.remove(taskId)
            return ResponseGetTaskStatus(TaskStatus.FAILED, null)
        }

        return when (response) {
            null -> {
                if (futureStatus == TaskStatus.QUESTION) {
                    val question = aIQuestionStorage.getQuestion(taskId)
                    ResponseGetTaskStatus(TaskStatus.QUESTION, null, message = question)
                } else {
                    ResponseGetTaskStatus(TaskStatus.ACTIVE, null)
                }
            }

            else -> {
                futureStorage.remove(taskId)
                ResponseGetTaskStatus(TaskStatus.COMPLETE, response)
            }
        }
    }
}
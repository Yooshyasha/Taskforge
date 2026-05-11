package com.yooshyasha.aiservice.service

import com.yooshyasha.aiservice.storage.AIQuestionStorage
import com.yooshyasha.aiservice.storage.FutureStatusStorage
import com.yooshyasha.aiservice.storage.FutureStorage
import com.yooshyasha.aiservice.storage.UserAnswerStorage
import dto.GeneratedTasksResponse
import dto.ResponseGetTaskStatus
import dto.ResponsePostGenerate
import dto.project.VikunjaProjectDTO
import enum.TaskStatus
import exceptions.ApiException
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.withTimeout
import org.springframework.stereotype.Service
import java.util.*
import kotlin.time.Duration.Companion.seconds

@Service
class GenerationService(
    private val aiTaskGenerationService: AITaskGenerationService,
    private val futureStorage: FutureStorage,
    private val futureStatusStorage: FutureStatusStorage,
    private val aiQuestionStorage: AIQuestionStorage,
    private val userAnswerStorage: UserAnswerStorage,
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
            taskId,
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
            futureStatusStorage.remove(taskId)
            return ResponseGetTaskStatus(TaskStatus.FAILED, null)
        }

        return when (response) {
            null -> {
                if (futureStatus == TaskStatus.QUESTION) {
                    val question = aiQuestionStorage.getQuestion(taskId)
                    ResponseGetTaskStatus(TaskStatus.QUESTION, null, message = question)
                } else {
                    ResponseGetTaskStatus(TaskStatus.ACTIVE, null)
                }
            }

            else -> {
                futureStorage.remove(taskId)
                futureStatusStorage.remove(taskId)
                ResponseGetTaskStatus(TaskStatus.COMPLETE, response)
            }
        }
    }

    suspend fun sendAnswer(taskId: UUID, answer: String): ResponseGetTaskStatus {
        userAnswerStorage.submitAnswer(taskId, answer)

        try {
            withTimeout(10.seconds) {
                while (getTask(taskId).status == TaskStatus.QUESTION) {
                    delay(1.seconds)
                }
            }
        } catch (e: TimeoutCancellationException) {
            throw ApiException("Статус не изменился по неизвестной причине", 500)
        }

        return getTask(taskId)
    }
}
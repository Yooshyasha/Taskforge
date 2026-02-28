package com.yooshyasha.aiservice.service

import com.yooshyasha.aiservice.dto.controller.ResponsePostGenerate
import dto.GeneratedTasksResponse
import dto.ResponseGetTaskStatus
import enum.TaskStatus
import kotlinx.coroutines.Deferred
import org.springframework.stereotype.Service
import java.util.*

@Service
class GenerationService(
    private val aiTaskGenerationService: AITaskGenerationService,
    private val futureStorageService: FutureStorageService,
) {
    fun generate(text: String): ResponsePostGenerate {
        val taskId = UUID.randomUUID()
        val task = aiTaskGenerationService.generation(text)
        futureStorageService.save(taskId, task)

        return ResponsePostGenerate(taskId)
    }

    suspend fun getTask(taskId: UUID): ResponseGetTaskStatus {
        val task: Deferred<GeneratedTasksResponse> = futureStorageService.getTask(taskId)
        val response: GeneratedTasksResponse?
        try {
            response = aiTaskGenerationService.getTaskResult(task)
        } catch (e: Exception) {
            return ResponseGetTaskStatus(TaskStatus.FAILED, null)
        }


        return when (response) {
            null -> ResponseGetTaskStatus(TaskStatus.ACTIVE, null)

            else -> ResponseGetTaskStatus(TaskStatus.COMPLETE, response)
        }
    }
}
package com.yooshyasha.aiservice.service

import dto.ResponsePostGenerate
import com.yooshyasha.aiservice.storage.FutureStorage
import dto.GeneratedTasksResponse
import dto.ResponseGetTaskStatus
import dto.project.VikunjaProjectDTO
import enum.TaskStatus
import kotlinx.coroutines.Deferred
import org.springframework.stereotype.Service
import java.util.*

@Service
class GenerationService(
    private val aiTaskGenerationService: AITaskGenerationService,
    private val futureStorage: FutureStorage,
) {
    fun generate(text: String, vikunjaProject: VikunjaProjectDTO?): ResponsePostGenerate {
        val taskId = UUID.randomUUID()
        val task = aiTaskGenerationService.generation(text)
        futureStorage.save(taskId, task)

        return ResponsePostGenerate(taskId)
    }

    suspend fun getTask(taskId: UUID): ResponseGetTaskStatus {
        val task: Deferred<GeneratedTasksResponse> = futureStorage.getTask(taskId)
        val response: GeneratedTasksResponse?
        try {
            response = aiTaskGenerationService.getTaskResult(task)
        } catch (e: Exception) {
            futureStorage.remove(taskId)
            return ResponseGetTaskStatus(TaskStatus.FAILED, null)
        }


        return when (response) {
            null -> ResponseGetTaskStatus(TaskStatus.ACTIVE, null)

            else -> {
                futureStorage.remove(taskId)
                ResponseGetTaskStatus(TaskStatus.COMPLETE, response)
            }
        }
    }
}
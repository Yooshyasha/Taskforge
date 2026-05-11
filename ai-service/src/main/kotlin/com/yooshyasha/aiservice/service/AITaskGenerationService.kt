package com.yooshyasha.aiservice.service

import com.yooshyasha.aiservice.ai.TaskManagerAgentProvider
import dto.GeneratedTasksResponse
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.UUID

@Service
class AITaskGenerationService(
    private val taskManagerAgentProvider: TaskManagerAgentProvider,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, e ->
        logger.error("Exception process ai agent:", e)
    })

    fun generation(text: String, isEdit: Boolean, futureId: UUID): Deferred<GeneratedTasksResponse> = scope.async {
        val agent = if (!isEdit) {
            taskManagerAgentProvider.provideAgent(futureId)
        } else {
            taskManagerAgentProvider.provideAgentWithEditMark(futureId)
        }
        return@async agent.run(text)
    }

    suspend fun getTaskResult(task: Deferred<GeneratedTasksResponse>): GeneratedTasksResponse? {
        return if (task.isCompleted) {
            task.await()
        } else null
    }
}
package com.yooshyasha.aiservice.service

import com.yooshyasha.aiservice.ai.TaskManagerAgentProvider
import dto.GeneratedTasksResponse
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class AITaskGenerationService(
    private val taskManagerAgentProvider: TaskManagerAgentProvider,
) {
    private val mutex = Mutex()
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, e ->
        logger.error("Exception process ai agent:", e)
    })

    fun generation(text: String): Deferred<GeneratedTasksResponse> = scope.async {
        mutex.withLock {
            return@async taskManagerAgentProvider.provideAgent().run(text)
        }
    }

    suspend fun getTaskResult(task: Deferred<GeneratedTasksResponse>): GeneratedTasksResponse? {
        return if (task.isActive) {
            null
        } else {
            task.await()
        }
    }
}
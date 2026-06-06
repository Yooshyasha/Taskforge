package com.yooshyasha.aiservice.service

import com.yooshyasha.aiservice.ai.TaskManagerAgentProvider
import dto.GeneratedTasksResponse
import enum.TaskDepth
import kotlinx.coroutines.*
import org.meeuw.i18n.languages.ISO_639_1_Code
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import java.util.*

@Service
class AITaskGenerationService(
    private val taskManagerAgentProvider: TaskManagerAgentProvider,
) {
    private val logger = LoggerFactory.getLogger(this::class.java)
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO + CoroutineExceptionHandler { _, e ->
        logger.error("Exception process ai agent:", e)
    })

    fun generation(
        text: String,
        isEdit: Boolean,
        futureId: UUID,
        language: ISO_639_1_Code,
        taskDepth: TaskDepth
    ): Deferred<GeneratedTasksResponse> = scope.async {
        val inputText = "$text\n\n" +
                "USER LANGUAGE: ${language.nameRecord()}\n" +
                "TASKS DEPTH: ${taskDepth.name}"
        val agent = if (!isEdit) {
            taskManagerAgentProvider.provideAgent(futureId)
        } else {
            taskManagerAgentProvider.provideAgentWithEditMark(futureId)
        }
        return@async agent.run(inputText)
    }

    suspend fun getTaskResult(task: Deferred<GeneratedTasksResponse>): GeneratedTasksResponse? {
        return if (task.isCompleted) {
            task.await()
        } else null
    }
}
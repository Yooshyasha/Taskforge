package com.yooshyasha.aiservice.ai.tools

import ai.koog.agents.core.tools.annotations.LLMDescription
import ai.koog.agents.core.tools.annotations.Tool
import ai.koog.agents.core.tools.reflect.ToolSet
import com.yooshyasha.aiservice.storage.AIQuestionStorage
import com.yooshyasha.aiservice.storage.FutureStatusStorage
import com.yooshyasha.aiservice.storage.UserAnswerStorage
import enum.TaskStatus
import kotlinx.coroutines.withTimeout
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import java.util.*
import java.util.concurrent.CancellationException
import kotlin.time.Duration.Companion.minutes


@Component
class UserInputToolSetFactory(
    private val futureStatusStorage: FutureStatusStorage,
    private val aiQuestionStorage: AIQuestionStorage,
    private val userAnswerStorage: UserAnswerStorage,
) {
    inner class UserInputToolSet(private val futureId: UUID) : ToolSet {
        private val logger = LoggerFactory.getLogger(UserInputToolSet::class.java)

        @Tool("require user input")
        @LLMDescription("Инструмент для запроса ввода пользователя")
        suspend fun requireUserInput(
            @LLMDescription("Сообщение, которое увидит пользователь")
            message: String,
        ): String {
            try {
                futureStatusStorage.save(futureId, TaskStatus.QUESTION)
                aiQuestionStorage.save(futureId, message)

                val deferred = userAnswerStorage.subscribe(futureId)

                return withTimeout(5.minutes) {
                    deferred.await()
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                logger.error("Ошибка во время выполнения require user input ($futureId, $message)", e)
                return "Ошибка во время выполнения инструмента: ${e.message}"
            } finally {
                userAnswerStorage.remove(futureId)
                aiQuestionStorage.remove(futureId)
                futureStatusStorage.save(futureId, TaskStatus.ACTIVE)
            }
        }
    }
}
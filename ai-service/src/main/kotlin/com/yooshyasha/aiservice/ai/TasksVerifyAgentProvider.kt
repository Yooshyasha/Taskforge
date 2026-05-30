package com.yooshyasha.aiservice.ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.functionalStrategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.params.LLMParams
import com.yooshyasha.aiservice.ai.base.BaseAgentProvider
import com.yooshyasha.aiservice.dto.ai.VerifyTasksResult
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.util.*

@Component
class TasksVerifyAgentProvider(
    @Qualifier("aiExecutor") private val aiExecutor: MultiLLMPromptExecutor,
    private val verifySystemPrompt: String,
    private val modelResolver: ModelResolver,
) : BaseAgentProvider<String, VerifyTasksResult> {
    override suspend fun provideAgent(futureId: UUID): AIAgent<String, VerifyTasksResult> {
        return provideAgent(verifySystemPrompt, futureId)
    }

    override suspend fun provideAgent(
        systemPrompt: String,
        futureId: UUID
    ): AIAgent<String, VerifyTasksResult> {
        val strategy = functionalStrategy<String, VerifyTasksResult> { input ->
            return@functionalStrategy requestLLMStructured<VerifyTasksResult>(
                "MODE: VERIFY\n$input"
            ).onFailure { throw Exception("structure failed") }.getOrNull()!!.data
        }

        return AIAgent(
            promptExecutor = aiExecutor,
            strategy = strategy,
            agentConfig = AIAgentConfig(
                prompt = prompt("verify agent", params = LLMParams(maxTokens = 64_000)) {
                    system(systemPrompt)
                },
                model = modelResolver.resolve(),
                maxAgentIterations = 32,
            ),
            toolRegistry = ToolRegistry.EMPTY,
        )
    }
}
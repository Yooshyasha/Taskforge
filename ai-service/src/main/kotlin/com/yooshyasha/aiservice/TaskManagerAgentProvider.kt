package com.yooshyasha.aiservice

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import com.yooshyasha.aiservice.ai.base.BaseAgentProvider
import dto.GeneratedTasksResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class TaskManagerAgentProvider(
    @Qualifier("aiExecutor") private val aiExecutor: SingleLLMPromptExecutor,
) : BaseAgentProvider<String, GeneratedTasksResponse> {
    override fun provideAgent(): AIAgent<String, GeneratedTasksResponse> {
        val strategy = strategy<String, GeneratedTasksResponse>("task manager") {

        }

        return AIAgent(
            executor = aiExecutor,
            strategy = strategy,
            toolRegistry = ToolRegistry.EMPTY,
        )
    }
}
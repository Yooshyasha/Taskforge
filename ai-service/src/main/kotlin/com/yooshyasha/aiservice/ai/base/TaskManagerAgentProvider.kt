package com.yooshyasha.aiservice.ai.base

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.llm.LLModel
import dto.GeneratedTasksResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class TaskManagerAgentProvider(
    @Qualifier("aiExecutor") private val aiExecutor: SingleLLMPromptExecutor,
    private val llModel: LLModel,
) : BaseAgentProvider<String, GeneratedTasksResponse> {
    override fun provideAgent(): AIAgent<String, GeneratedTasksResponse> {
        val strategy = strategy<String, GeneratedTasksResponse>("task manager") {

        }

        return AIAgent(
            promptExecutor = aiExecutor,
            strategy = strategy,
            agentConfig = AIAgentConfig(
                prompt = prompt("task manager agent prompt") {
                    system("")
                },
                model = llModel,
                maxAgentIterations = 50,
            ),
            toolRegistry = ToolRegistry.EMPTY,
        )
    }
}
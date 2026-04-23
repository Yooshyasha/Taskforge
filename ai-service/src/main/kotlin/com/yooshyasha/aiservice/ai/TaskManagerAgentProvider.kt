package com.yooshyasha.aiservice.ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.entity.createStorageKey
import ai.koog.agents.core.dsl.builder.forwardTo
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLMRequestStructured
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.params.LLMParams
import com.yooshyasha.aiservice.ai.base.BaseAgentProvider
import com.yooshyasha.aiservice.dto.ai.VerifyTasksResult
import com.yooshyasha.aiservice.enum.VerifyStatus
import dto.GeneratedTasksResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
class TaskManagerAgentProvider(
    @param:Qualifier("aiExecutor") private val aiExecutor: SingleLLMPromptExecutor,
    private val llModel: LLModel,
    private val defaultSystemPrompt: String,
    private val editMarkSystemPrompt: String,
) : BaseAgentProvider<String, GeneratedTasksResponse> {
    override fun provideAgent(systemPrompt: String): AIAgent<String, GeneratedTasksResponse> {
        val strategy = strategy<String, GeneratedTasksResponse>("task manager") {
            val originalKey = createStorageKey<String>("original")
            val generatedTasksKey = createStorageKey<GeneratedTasksResponse>("generated tasks response")

            val nodeGenerateTasks by nodeLLMRequestStructured<GeneratedTasksResponse>("generate")
            val nodeVerify by nodeLLMRequestStructured<VerifyTasksResult>("verify")

            edge(nodeStart forwardTo nodeGenerateTasks transformed {
                storage.set(originalKey, it)
                """
                    MODE: GENERATE
                    ORIGINAL:
                    $it
                    REFINEMENT:
                    none
                """.trimMargin()
            })
            edge(nodeGenerateTasks forwardTo nodeVerify transformed {
                it
                    .onSuccess { response -> storage.set(generatedTasksKey, response.data) }
                    .onFailure { throw Exception("structure failed") }
                val original = storage.get(originalKey)!!

                """
                    MODE: VERIFY
                    ORIGINAL:
                    $original
                    GENERATED:
                    ${it.getOrNull()!!.data}
                """.trimIndent()
            })

            edge(nodeVerify forwardTo nodeGenerateTasks onCondition {
                it.onFailure { throw Exception("structure failed") }

                it.getOrNull()!!.data.status == VerifyStatus.FAIL
            } transformed {
                val original = storage.get(originalKey)!!
                """
                    MODE: GENERATE
                    ORIGINAL: 
                    $original
                    REFINEMENT:
                    ${it.getOrNull()!!.data.refinementInstruction}
                """.trimIndent()
            })
            edge(nodeVerify forwardTo nodeFinish onCondition {
                it.onFailure { throw Exception("structure failed") }

                it.getOrNull()!!.data.status == VerifyStatus.OK
            } transformed {
                storage.get(generatedTasksKey)!!
            })
        }

        return AIAgent(
            promptExecutor = aiExecutor,
            strategy = strategy,
            agentConfig = AIAgentConfig(
                prompt = prompt("task manager agent prompt", params = LLMParams(maxTokens = 64_000)) {
                    system(systemPrompt)
                },
                model = llModel,
                maxAgentIterations = 12,
            ),
            toolRegistry = ToolRegistry.EMPTY,
        )
    }

    override fun provideAgent() = provideAgent(defaultSystemPrompt)
    fun provideAgentWithEditMark() = provideAgent(editMarkSystemPrompt)
}
package com.yooshyasha.aiservice.ai

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.agent.config.AIAgentConfig
import ai.koog.agents.core.agent.entity.createStorageKey
import ai.koog.agents.core.dsl.builder.node
import ai.koog.agents.core.dsl.builder.strategy
import ai.koog.agents.core.dsl.extension.nodeLLMRequestStructured
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.agents.ext.agent.subgraphWithTask
import ai.koog.prompt.dsl.prompt
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.params.LLMParams
import com.yooshyasha.aiservice.ai.base.BaseAgentProvider
import com.yooshyasha.aiservice.ai.tools.UserInputToolSetFactory
import com.yooshyasha.aiservice.dto.ai.VerifyTasksResult
import com.yooshyasha.aiservice.enum.VerifyStatus
import dto.GeneratedTasksResponse
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import java.util.*

@Component
class TaskManagerAgentProvider(
    @Qualifier("aiExecutor") private val aiExecutor: MultiLLMPromptExecutor,
    private val defaultSystemPrompt: String,
    private val editMarkSystemPrompt: String,
    private val userInputToolSetFactory: UserInputToolSetFactory,
    private val tasksVerifyAgentProvider: TasksVerifyAgentProvider,
    private val modelResolver: ModelResolver,
) : BaseAgentProvider<String, GeneratedTasksResponse> {
    override suspend fun provideAgent(systemPrompt: String, futureId: UUID): AIAgent<String, GeneratedTasksResponse> {
        val strategy = strategy<String, GeneratedTasksResponse>("task manager") {
            val originalKey = createStorageKey<String>("original")
            val clarificationsKey = createStorageKey<String>("clarifications")
            val generatedTasksKey = createStorageKey<GeneratedTasksResponse>("generated tasks response")

            val nodeVerifyInput by subgraphWithTask<String, String>(
                tools = ToolRegistry {
                    tools(userInputToolSetFactory.UserInputToolSet(futureId))
                }.tools,
                assistantResponseRepeatMax = 12,
            ) { userInput ->
                "Определи, достаточно ли контекста для определения задач. Запроси уточнения у пользователя с " +
                        "помощью инструмента по необходимости. " +
                        "Итого ТЗ должно быть максимально понятно.\nЗапрос пользователя: $userInput"
            }
            val nodeGenerateTasks by nodeLLMRequestStructured<GeneratedTasksResponse>("generate")
            val nodeVerify by node<String, VerifyTasksResult>("verify") { input ->
                val original = storage.get(originalKey)
                val clarifications = storage.get(clarificationsKey)

                tasksVerifyAgentProvider.provideAgent(futureId).run(
                    """
                        GENERATED TASKS:
                        $input
                        ORIGINAL:
                        $original
                        CLARIFICATIONS:
                        $clarifications
                    """.trimIndent()
                )
            }

            edge(nodeStart forwardTo nodeVerifyInput transformed {
                storage.set(originalKey, it)

                it
            })
            edge(nodeVerifyInput forwardTo nodeGenerateTasks transformed {
                storage.set(clarificationsKey, it)

                """
                    MODE: GENERATE
                """.trimMargin()
            })

            edge(nodeGenerateTasks forwardTo nodeVerify transformed {
                it
                    .onSuccess { response -> storage.set(generatedTasksKey, response.data) }
                    .onFailure { throw Exception("structure failed") }

                it.getOrNull()!!.data.toString()
            })

            edge(nodeVerify forwardTo nodeGenerateTasks onCondition {
                it.status == VerifyStatus.FAIL
            } transformed {
                """
                    MODE: GENERATE
                    REFINEMENT:
                    ${it.refinementInstruction}
                """.trimIndent()
            })
            edge(nodeVerify forwardTo nodeFinish onCondition {
                it.status == VerifyStatus.OK
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
                model = modelResolver.resolve(),
                maxAgentIterations = 32,
            ),
            toolRegistry = ToolRegistry.EMPTY,
        )
    }

    override suspend fun provideAgent(futureId: UUID) = provideAgent(defaultSystemPrompt, futureId)
    suspend fun provideAgentWithEditMark(futureId: UUID) =
        provideAgent(defaultSystemPrompt + editMarkSystemPrompt, futureId)
}
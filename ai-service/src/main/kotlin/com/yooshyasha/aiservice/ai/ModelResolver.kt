package com.yooshyasha.aiservice.ai

import ai.koog.prompt.executor.clients.anthropic.AnthropicModels
import ai.koog.prompt.executor.clients.bedrock.BedrockModels
import ai.koog.prompt.executor.clients.deepseek.DeepSeekModels
import ai.koog.prompt.executor.clients.google.GoogleModels
import ai.koog.prompt.executor.clients.mistralai.MistralAIModels
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.clients.openrouter.OpenRouterModels
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import ai.koog.prompt.executor.ollama.client.OllamaModels
import ai.koog.prompt.llm.LLModel
import kotlinx.coroutines.withTimeoutOrNull
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.concurrent.atomic.AtomicReference
import kotlin.time.Duration.Companion.seconds

@Component
class ModelResolver(
    @Qualifier("aiExecutor") private val aiExecutor: MultiLLMPromptExecutor,
    @Value($$"${ai.model.id}") private val aiModelId: String,
) {
    private val cached = AtomicReference<LLModel?>(null)

    private val staticModels: List<LLModel> = listOf(
        OpenAIModels, AnthropicModels,
        GoogleModels, DeepSeekModels,
        MistralAIModels, OpenRouterModels,
        OllamaModels, BedrockModels,
    ).flatMap { it.models }

    suspend fun resolve(): LLModel {
        cached.get()?.let { return it }

        val model = staticModels.firstOrNull { it.id == aiModelId }
            ?: withTimeoutOrNull(10.seconds) {
                aiExecutor.models().firstOrNull { it.id == aiModelId }
            }
            ?: throw IllegalStateException(
                "Model '$aiModelId' not found (static + remote, или таймаут)"
            )

        cached.compareAndSet(null, model)
        return cached.get()!!
    }
}
package com.yooshyasha.aiservice.config

import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.llm.LLMCapability
import ai.koog.prompt.llm.LLMProvider
import ai.koog.prompt.llm.LLModel
import ai.koog.prompt.llm.OllamaModels
import org.springframework.beans.factory.BeanCreationException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader

@Configuration
class AIAgentConfig(
    @Qualifier("openAIExecutor") private val openaiAIExecutor: SingleLLMPromptExecutor?,
    @Qualifier("anthropicExecutor") private val anthropicAIExecutor: SingleLLMPromptExecutor?,
    @Qualifier("googleExecutor") private val googleAIExecutor: SingleLLMPromptExecutor?,
    @Qualifier("ollamaExecutor") private val ollamaAIExecutor: SingleLLMPromptExecutor?,
    @Qualifier("openRouterExecutor") private val openRouterAIExecutor: SingleLLMPromptExecutor?,
    @Qualifier("deepSeekExecutor") private val deepSeekAIExecutor: SingleLLMPromptExecutor?,
    @Value("ai.model.id") private val aiModelId: String,
    private val resourceLoader: ResourceLoader,
) {
    @Bean
    fun aiExecutor(): SingleLLMPromptExecutor {
        return when {
            openaiAIExecutor != null -> openaiAIExecutor
            anthropicAIExecutor != null -> anthropicAIExecutor
            googleAIExecutor != null -> googleAIExecutor
            ollamaAIExecutor != null -> ollamaAIExecutor
            openRouterAIExecutor != null -> openRouterAIExecutor
            deepSeekAIExecutor != null -> deepSeekAIExecutor
            else -> throw BeanCreationException("Zero available executors")
        }
    }

    @Bean
    fun llModel(aiExecutor: SingleLLMPromptExecutor): LLModel {
        val provider = when {
            openaiAIExecutor != null -> LLMProvider.OpenAI
            anthropicAIExecutor != null -> LLMProvider.Anthropic
            googleAIExecutor != null -> LLMProvider.Google
            ollamaAIExecutor != null -> LLMProvider.Ollama
            openRouterAIExecutor != null -> LLMProvider.OpenRouter
            deepSeekAIExecutor != null -> LLMProvider.DeepSeek
            else -> throw BeanCreationException("Zero available executors")
        }

        OllamaModels

        return LLModel(
            provider = provider,
            id = aiModelId,
            capabilities = listOf(LLMCapability.Temperature, LLMCapability.Completion),
            contextLength = 32_000,
        )
    }

    @Bean
    fun systemPrompt(): String = resourceLoader.getResource("system_prompt.txt").file.readText()
}
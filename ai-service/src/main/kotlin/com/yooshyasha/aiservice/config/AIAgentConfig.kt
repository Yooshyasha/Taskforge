package com.yooshyasha.aiservice.config

import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.SingleLLMPromptExecutor
import ai.koog.prompt.llm.LLModel
import io.ktor.client.*
import org.springframework.beans.factory.BeanCreationException
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader

@Configuration
class AIAgentConfig(
    @param:Qualifier("openAIExecutor") private val openaiAIExecutor: SingleLLMPromptExecutor?,
    @param:Value($$"${ai.koog.openai.api-key}") private val openaiApiKey: String,
//    @param:Qualifier("anthropicExecutor") private val anthropicAIExecutor: SingleLLMPromptExecutor?,
//    @param:Qualifier("googleExecutor") private val googleAIExecutor: SingleLLMPromptExecutor?,
//    @param:Qualifier("ollamaExecutor") private val ollamaAIExecutor: SingleLLMPromptExecutor?,
//    @param:Qualifier("openRouterExecutor") private val openRouterAIExecutor: SingleLLMPromptExecutor?,
//    @param:Qualifier("deepSeekExecutor") private val deepSeekAIExecutor: SingleLLMPromptExecutor?,
    @param:Value($$"${ai.model.id}") private val aiModelId: String,
    private val resourceLoader: ResourceLoader,
) {
    @Bean
    fun aiExecutor(): SingleLLMPromptExecutor {
        return when {
            openaiAIExecutor != null -> SingleLLMPromptExecutor(
                OpenAILLMClient(
                    apiKey = openaiApiKey,
                    baseClient = HttpClient(),
                )
            )
//            anthropicAIExecutor != null -> anthropicAIExecutor
//            googleAIExecutor != null -> googleAIExecutor
//            ollamaAIExecutor != null -> ollamaAIExecutor
//            openRouterAIExecutor != null -> openRouterAIExecutor
//            deepSeekAIExecutor != null -> deepSeekAIExecutor
            else -> throw BeanCreationException("Zero available executors")
        }
    }

    @Bean
    fun llModel(aiExecutor: SingleLLMPromptExecutor): LLModel {
//        val provider = when {
//            openaiAIExecutor != null -> LLMProvider.OpenAI
//            anthropicAIExecutor != null -> LLMProvider.Anthropic
//            googleAIExecutor != null -> LLMProvider.Google
//            ollamaAIExecutor != null -> LLMProvider.Ollama
//            openRouterAIExecutor != null -> LLMProvider.OpenRouter
//            deepSeekAIExecutor != null -> LLMProvider.DeepSeek
//            else -> throw BeanCreationException("Zero available executors")
//        }
//
//        return LLModel(
//            provider = provider,
//            id = aiModelId,
//            capabilities = listOf(LLMCapability.Temperature, LLMCapability.Completion),
//            contextLength = 32_000,
//        )
        return OpenAIModels.Chat.GPT4_1
    }

    @Bean
    fun systemPrompt(): String =
        resourceLoader
            .getResource("classpath:system_prompt.txt")
            .inputStream
            .bufferedReader()
            .readText()
}
package com.yooshyasha.aiservice.config

import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.io.ResourceLoader

@Configuration
class AIAgentConfig(
    @Qualifier("multiLLMPromptExecutor") private val multiLLMPromptExecutor: MultiLLMPromptExecutor,
    private val resourceLoader: ResourceLoader,
) {
    // Обертка обусловлена возможностью дальшейшей модификации
    @Bean
    fun aiExecutor(): MultiLLMPromptExecutor {
        return multiLLMPromptExecutor
    }

    @Bean
    fun defaultSystemPrompt(): String =
        resourceLoader
            .getResource("classpath:default_system_prompt.txt")
            .inputStream
            .bufferedReader()
            .readText()

    @Bean
    fun editMarkSystemPrompt(): String =
        resourceLoader
            .getResource("classpath:edit_mark_system_prompt.txt")
            .inputStream
            .bufferedReader()
            .readText()

    @Bean
    fun verifySystemPrompt(): String =
        resourceLoader
            .getResource("classpath:verify_system_prompt.txt")
            .inputStream
            .bufferedReader()
            .readText()
}
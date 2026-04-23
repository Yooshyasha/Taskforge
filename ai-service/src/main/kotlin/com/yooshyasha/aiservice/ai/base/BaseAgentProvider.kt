package com.yooshyasha.aiservice.ai.base

import ai.koog.agents.core.agent.AIAgent
import dto.GeneratedTasksResponse

interface BaseAgentProvider<I, R> {
    fun provideAgent(): AIAgent<I, R>
    fun provideAgent(systemPrompt: String): AIAgent<I, R>
}
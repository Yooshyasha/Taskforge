package com.yooshyasha.aiservice.ai.base

import ai.koog.agents.core.agent.AIAgent
import dto.GeneratedTasksResponse
import java.util.UUID

interface BaseAgentProvider<I, R> {
    fun provideAgent(futureId: UUID): AIAgent<I, R>
    fun provideAgent(systemPrompt: String, futureId: UUID): AIAgent<I, R>
}
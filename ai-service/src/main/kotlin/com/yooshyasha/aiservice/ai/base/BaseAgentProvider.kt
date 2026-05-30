package com.yooshyasha.aiservice.ai.base

import ai.koog.agents.core.agent.AIAgent
import dto.GeneratedTasksResponse
import java.util.UUID

interface BaseAgentProvider<I, R> {
    suspend fun provideAgent(futureId: UUID): AIAgent<I, R>
    suspend fun provideAgent(systemPrompt: String, futureId: UUID): AIAgent<I, R>
}
package com.yooshyasha.aiservice.dto.ai

import com.yooshyasha.aiservice.enum.VerifyStatus
import kotlinx.serialization.Serializable

@Serializable
data class VerifyTasksResult(
    val status: VerifyStatus,
    val issues: List<String>,
    val refinementInstruction: String?,
)

package com.yooshyasha.aiservice.service

import com.yooshyasha.aiservice.dto.controller.ResponsePostGenerate
import org.springframework.stereotype.Service
import java.util.*

@Service
class GenerationService(
    private val aiTaskGenerationService: AITaskGenerationService,
    private val taskStorageService: TaskStorageService,
) {
    fun generate(text: String): ResponsePostGenerate {
        val taskId = UUID.randomUUID()
        val task = aiTaskGenerationService.generation(text)
        taskStorageService.save(taskId, task)

        return ResponsePostGenerate(taskId)
    }
}
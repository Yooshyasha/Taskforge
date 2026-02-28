package com.yooshyasha.aiservice.controller

import dto.ResponsePostGenerate
import com.yooshyasha.aiservice.service.GenerationService
import dto.GenerateRequest
import dto.ResponseGetTaskStatus
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController("/v1/api/generation")
class GenerationController(
    private val generationService: GenerationService,
) {
    @PostMapping("/")
    fun generate(@RequestBody data: GenerateRequest): ResponsePostGenerate {
        return generationService.generate(data.text)
    }

    @GetMapping("/{taskId}")
    suspend fun getTask(@PathVariable taskId: UUID): ResponseGetTaskStatus {
        return generationService.getTask(taskId)
    }
}
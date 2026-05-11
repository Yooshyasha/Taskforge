package com.yooshyasha.aiservice.controller

import com.yooshyasha.aiservice.service.GenerationService
import dto.GenerateRequest
import dto.RequestSendAnswer
import dto.ResponseGetTaskStatus
import dto.ResponsePostGenerate
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/v1/api/generation")
class GenerationController(
    private val generationService: GenerationService,
) {
    @PostMapping
    fun generate(@RequestBody data: GenerateRequest): ResponsePostGenerate {
        return generationService.generate(data.text, data.vikunjaProject)
    }

    @GetMapping("/{taskId}")
    suspend fun getTask(@PathVariable taskId: UUID): ResponseGetTaskStatus {
        return generationService.getTask(taskId)
    }

    @PostMapping("/answer")
    suspend fun sendAnswer(@RequestBody data: RequestSendAnswer): ResponseGetTaskStatus {
        return generationService.sendAnswer(data.taskId, data.answer)
    }
}
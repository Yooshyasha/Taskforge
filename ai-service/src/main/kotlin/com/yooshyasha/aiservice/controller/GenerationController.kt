package com.yooshyasha.aiservice.controller

import com.yooshyasha.aiservice.dto.controller.ResponsePostGenerate
import com.yooshyasha.aiservice.service.GenerationService
import dto.GenerateRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController("/v1/api/generation")
class GenerationController(
    private val generationService: GenerationService,
) {
    @PostMapping("/")
    fun generate(@RequestBody data: GenerateRequest): ResponsePostGenerate {
        return generationService.generate(data.text)
    }
}
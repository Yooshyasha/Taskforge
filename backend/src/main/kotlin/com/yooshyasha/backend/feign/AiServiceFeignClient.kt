package com.yooshyasha.backend.feign

import dto.GenerateRequest
import dto.ResponseGetTaskStatus
import dto.ResponsePostGenerate
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import java.util.*

@FeignClient(name = "aiServiceClient", url = "http://ai-service:8080")
interface AiServiceFeignClient {
    @PostMapping("/v1/api/generation")
    fun generate(@RequestBody request: GenerateRequest): ResponsePostGenerate

    @GetMapping("/v1/api/generation/{taskId}")
    fun getTask(@PathVariable taskId: UUID): ResponseGetTaskStatus
}
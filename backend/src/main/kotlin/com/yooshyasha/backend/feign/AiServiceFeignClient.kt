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

@FeignClient(name = "aiServiceClient", url = "http://localhost:8080")
interface AiServiceFeignClient {
    @PostMapping("/")
    fun generate(@RequestBody request: GenerateRequest): ResponsePostGenerate

    @GetMapping("/{taskId}")
    fun getTask(@PathVariable taskId: UUID): ResponseGetTaskStatus
}
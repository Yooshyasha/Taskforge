package com.yooshyasha.backend.feign

import com.yooshyasha.backend.config.VikunjaFeignConfig
import com.yooshyasha.backend.dto.api.*
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(
    name = "vikunjaClient",
    url = "\${vikunja.url}",
    configuration = [VikunjaFeignConfig::class]
)
interface VikunjaClient {
    @PutMapping("/projects", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createProject(
        @RequestHeader("Authorization") auth: String,
        @RequestBody request: ProjectRequest
    ): ProjectResponse

    @PutMapping("/projects/{projectId}/tasks", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createTask(
        @RequestHeader("Authorization") auth: String,
        @RequestBody request: TaskRequest,
        @PathVariable projectId: Int,
    ): TaskResponse

    @PutMapping("/tasks/{taskId}/comments", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createTaskComment(
        @RequestHeader("Authorization") auth: String,
        @PathVariable taskId: Int,
        @RequestBody request: TaskCommentRequest
    ): TaskCommentResponse
}
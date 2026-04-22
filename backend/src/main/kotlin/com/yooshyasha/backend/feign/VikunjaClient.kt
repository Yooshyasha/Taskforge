package com.yooshyasha.backend.feign

import com.yooshyasha.backend.config.VikunjaFeignConfig
import com.yooshyasha.backend.dto.api.*
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
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
        @RequestBody request: ProjectRequest
    ): ProjectResponse

    @PutMapping("/projects/{projectId}/tasks", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createTask(
        @RequestBody request: TaskRequest,
        @PathVariable projectId: Int,
    ): TaskResponse

    @PutMapping("/tasks/{taskId}/comments", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun createTaskComment(
        @PathVariable taskId: Int,
        @RequestBody request: TaskCommentRequest
    ): TaskCommentResponse

    @PutMapping("/labels")
    fun createLabel(
        @RequestBody request: LabelRequest,
    ): LabelResponse

    @PutMapping("/tasks/{taskId}/labels")
    fun addLabelToTask(
        @PathVariable taskId: Int,
        @RequestBody request: AddLabelRequest,
    ): AddLabelResponse

    @GetMapping("/projects")
    fun getProjects(): List<ProjectResponse>

    @GetMapping("/projects/{projectId}/tasks")
    fun getProjectTasks(
        @PathVariable projectId: Int,
    ): List<TaskResponse>

    @GetMapping("/projects/{projectId}/buckets")
    fun getProjectBuckets(
        @PathVariable projectId: Int,
    ): List<BucketResponse>

    @PostMapping("/tasks/{taskId}", consumes = [MediaType.APPLICATION_JSON_VALUE])
    fun updateTask(
        @PathVariable taskId: Int,
        @RequestBody request: TaskUpdateRequest,
    ): TaskResponse

    @DeleteMapping("/tasks/{taskId}")
    fun deleteTask(
        @PathVariable taskId: Int,
    ): DeleteResponse
}
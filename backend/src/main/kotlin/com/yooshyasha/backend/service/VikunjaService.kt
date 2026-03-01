package com.yooshyasha.backend.service

import com.yooshyasha.backend.config.VikunjaConfig
import com.yooshyasha.backend.dto.api.*
import com.yooshyasha.backend.feign.VikunjaClient
import org.springframework.stereotype.Service

@Service
class VikunjaService(
    private val vikunjaClient: VikunjaClient,
    private val config: VikunjaConfig
) {
    fun createProject(title: String): ProjectResponse {
        val request = ProjectRequest(title = title)
        return vikunjaClient.createProject(config.vikunjaAuthorization(), request)
    }

    fun createTask(projectId: Int, title: String, description: String, labels: List<String>): TaskResponse {
        val request = TaskRequest(
            title = title,
            description = description,
            labels = labels.map { TaskLabel(it) }
        )
        return vikunjaClient.createTask(config.vikunjaAuthorization(), request, projectId)
    }

    fun addCommentToTask(taskId: Int, text: String): TaskCommentResponse {
        val request = TaskCommentRequest(comment = text)
        return vikunjaClient.createTaskComment(config.vikunjaAuthorization(), taskId, request)
    }
}
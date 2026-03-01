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

    fun createTask(projectId: Int, title: String, description: String): TaskResponse {
        val request = TaskRequest(
            title = title,
            description = description,
        )
        return vikunjaClient.createTask(config.vikunjaAuthorization(), request, projectId)
    }

    fun addCommentToTask(taskId: Int, text: String): TaskCommentResponse {
        val request = TaskCommentRequest(comment = text)
        return vikunjaClient.createTaskComment(config.vikunjaAuthorization(), taskId, request)
    }

    fun createLabel(title: String): LabelResponse {
        val request = LabelRequest(title = title)
        return vikunjaClient.createLabel(config.vikunjaAuthorization(), request)
    }

    fun addLabelToTask(taskId: Int, labelId: Int): AddLabelResponse {
        val request = AddLabelRequest(label_id = labelId)
        return vikunjaClient.addLabelToTask(config.vikunjaAuthorization(), taskId, request)
    }
}
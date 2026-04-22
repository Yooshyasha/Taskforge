package com.yooshyasha.backend.service

import com.yooshyasha.backend.config.VikunjaConfig
import com.yooshyasha.backend.dto.api.*
import com.yooshyasha.backend.exceptions.ProjectNotFound
import com.yooshyasha.backend.feign.VikunjaClient
import dto.TaskDTO
import dto.project.VikunjaProjectDTO
import dto.project.VikunjaTaskDTO
import enum.project.VikunjaTaskStatus
import org.springframework.stereotype.Service
import kotlin.random.Random

@Service
class VikunjaService(
    private val vikunjaClient: VikunjaClient,
    private val config: VikunjaConfig
) {
    fun createProject(title: String): ProjectResponse {
        val request = ProjectRequest(title = title)
        return vikunjaClient.createProject(request)
    }

    fun createTask(projectId: Int, title: String, description: String): TaskResponse {
        val request = TaskRequest(
            title = title,
            description = description,
        )
        return vikunjaClient.createTask(request, projectId)
    }

    fun addCommentToTask(taskId: Int, text: String): TaskCommentResponse {
        val request = TaskCommentRequest(comment = text)
        return vikunjaClient.createTaskComment(taskId, request)
    }

    fun createLabel(title: String): LabelResponse {
        val request = LabelRequest(title = title)
        return vikunjaClient.createLabel(request)
    }

    fun addLabelToTask(taskId: Int, labelId: Int): AddLabelResponse {
        val request = AddLabelRequest(label_id = labelId, hex_color = generateHexColor())
        return vikunjaClient.addLabelToTask(taskId, request)
    }

    private fun generateHexColor(): String {
        val r = Random.nextInt(0, 256)
        val g = Random.nextInt(0, 256)
        val b = Random.nextInt(0, 256)
        return String.format("#%02X%02X%02X", r, g, b)
    }

    fun getProjectTasks(projectId: Int): List<VikunjaTaskDTO> {
        val response = vikunjaClient.getProjectTasks(projectId)

        return response.map { task ->
            val status = if (task.done) VikunjaTaskStatus.COMPLETE else VikunjaTaskStatus.TODO
            VikunjaTaskDTO(task.id, task.title, task.description.orEmpty(), status)
        }
    }

    fun getProjects(): List<VikunjaProjectDTO> {
        val response = vikunjaClient.getProjects()

        return response.map { project ->
            VikunjaProjectDTO(
                project.id, project.title, tasks = getProjectTasks(project.id)
            )
        }
    }

    fun getProject(projectId: Int): VikunjaProjectDTO {
        try {
            return getProjects().first { it.id == projectId }
        } catch (e: NoSuchElementException) {
            throw ProjectNotFound()
        }
    }

    fun updateTask(taskId: Int, task: TaskDTO): TaskResponse {
        val request = TaskUpdateRequest(
            title = task.name, description = task.description, done = task.done,
        )

        return vikunjaClient.updateTask(taskId, request)
    }

    fun deleteTask(taskId: Int): DeleteResponse {
        return vikunjaClient.deleteTask(taskId)
    }
}
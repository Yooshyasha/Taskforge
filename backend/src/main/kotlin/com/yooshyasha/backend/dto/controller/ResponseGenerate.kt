package com.yooshyasha.backend.dto.controller

import dto.GeneratedTasksResponse
import dto.ResponseGetTaskStatus
import dto.project.VikunjaTaskDTO
import enum.TaskStatus

data class ResponseGenerate(
    val status: TaskStatus,
    val message: String? = null,
    val projectId: Int?,
    val generatedTasks: GeneratedTasksResponse?,
    val editMap: List<VikunjaTaskDTO>?,  // оригинал
) {
    constructor(response: ResponseGetTaskStatus, editMap: List<VikunjaTaskDTO>?, projectId: Int?) : this(
        status = response.status,
        message = response.message,
        projectId = projectId,
        generatedTasks = response.generatedTasks,
        editMap = editMap,
    )
}

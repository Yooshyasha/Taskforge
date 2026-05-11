package dto

import enum.TaskStatus

data class ResponseGetTaskStatus(
    val status: TaskStatus,
    val generatedTasks: GeneratedTasksResponse?,
    val message: String? = null,
)

package dto

import kotlinx.serialization.Serializable

@Serializable
data class GeneratedTasksResponse(
    val tasks: List<TaskDTO>,
    val projectName: String,
)

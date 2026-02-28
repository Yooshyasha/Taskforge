package dto

import kotlinx.serialization.Serializable

@Serializable
data class TaskDTO(
    val name: String,
    val description: String,
    val comments: List<String>?,
    val tags: List<String>,
)

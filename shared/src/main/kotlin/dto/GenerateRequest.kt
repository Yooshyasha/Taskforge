package dto

import dto.project.VikunjaProjectDTO
import enum.TaskDepth

data class GenerateRequest(
    val text: String,
    val vikunjaProject: VikunjaProjectDTO?,
    val language: String,
    val taskDepth: TaskDepth,
)

package dto

import dto.project.VikunjaProjectDTO

data class GenerateRequest(
    val text: String,
    val vikunjaProject: VikunjaProjectDTO?,
)

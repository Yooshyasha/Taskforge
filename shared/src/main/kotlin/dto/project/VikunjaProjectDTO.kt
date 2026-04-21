package dto.project

data class VikunjaProjectDTO(
    val id: Int,
    val tasks: List<VikunjaTaskDTO>,
)
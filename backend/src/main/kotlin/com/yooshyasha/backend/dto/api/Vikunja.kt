package com.yooshyasha.backend.dto.api

data class ProjectRequest(
    val title: String,
    val description: String? = null,
    val hex_color: String? = null,
    val identifier: String? = null,
    val parent_project_id: Int? = null
)

data class ProjectResponse(
    val id: Int,
    val title: String,
    val description: String?,
    val hex_color: String?,
    val identifier: String?,
    val parent_project_id: Int?,
    val is_archived: Boolean,
    val is_favorite: Boolean
)

data class TaskLabel(
    val title: String,
)

data class TaskRequest(
    val title: String,
    val description: String? = null,
    val labels: List<TaskLabel>? = null
)

data class TaskResponse(
    val id: Int,
    val title: String,
    val description: String?,
    val project_id: Int,
    val done: Boolean
)

data class TaskCommentRequest(
    val comment: String
)

data class TaskCommentResponse(
    val id: Int,
    val comment: String,
    val created: String,
    val updated: String
)
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
)

data class TaskUpdateRequest(
    val title: String? = null,
    val description: String? = null,
    val done: Boolean? = null,
    val due_date: String? = null,
    val priority: Int? = null,
    val bucket_id: Int? = null,
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

data class LabelRequest(
    val title: String,
)

data class LabelResponse(
    val id: Int,
    val title: String,
)

data class AddLabelRequest(
    val label_id: Int,
    val hex_color: String,
)

data class AddLabelResponse(
    val label_id: Int,
)

data class BucketResponse(
    val id: Int,
    val title: String,
    val project_view_id: Int,
    val tasks: List<TaskResponse>?,
    val limit: Int?,
    val is_done_bucket: Boolean?,
    val position: Double?,
    val count: Int?,
)
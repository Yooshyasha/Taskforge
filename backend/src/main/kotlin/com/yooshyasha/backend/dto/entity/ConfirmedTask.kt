package com.yooshyasha.backend.dto.entity

import com.yooshyasha.backend.enum.ConfirmedTaskStatus
import dto.TaskDTO

data class ConfirmedTask(
    val status: ConfirmedTaskStatus,
    val taskDTO: TaskDTO?,
)

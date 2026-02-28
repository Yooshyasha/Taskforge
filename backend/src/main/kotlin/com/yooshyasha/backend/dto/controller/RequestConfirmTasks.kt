package com.yooshyasha.backend.dto.controller

import com.yooshyasha.backend.dto.entity.ConfirmedTask

data class RequestConfirmTasks(
    val confirmTasks: List<ConfirmedTask>,
)

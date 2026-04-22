package com.yooshyasha.backend.dto.controller

import dto.project.VikunjaProjectDTO

data class ResponseGetProjects(
    val projects: List<VikunjaProjectDTO>,
)

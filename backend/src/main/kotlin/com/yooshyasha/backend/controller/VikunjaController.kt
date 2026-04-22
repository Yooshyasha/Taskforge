package com.yooshyasha.backend.controller

import com.yooshyasha.backend.dto.controller.ResponseGetProjects
import com.yooshyasha.backend.service.VikunjaService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/vikunja")
class VikunjaController(private val vikunjaService: VikunjaService) {
    @GetMapping("/projects")
    fun getProjects(): ResponseGetProjects {
        return ResponseGetProjects(vikunjaService.getProjects())
    }
}
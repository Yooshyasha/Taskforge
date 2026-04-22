package com.yooshyasha.backend.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class VikunjaConfig(
    @Value("\${vikunja.url}") private val vikunjaUrl: String,
    @Value("\${vikunja.token}") private val vikunjaToken: String,
) {
    @Bean
    fun vikunjaAuthorization() = "Bearer $vikunjaToken"
}
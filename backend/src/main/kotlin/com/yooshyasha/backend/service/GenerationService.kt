package com.yooshyasha.backend.service

import com.yooshyasha.aiservice.dto.controller.ResponsePostGenerate
import com.yooshyasha.backend.feign.AiServiceFeignClient
import dto.GenerateRequest
import dto.ResponseGetTaskStatus
import enum.TaskStatus
import exceptions.ApiException
import exceptions.TaskNotFound
import org.springframework.stereotype.Service
import java.util.*

@Service
class GenerationService(
    private val aiServiceFeignClient: AiServiceFeignClient,
) {
    fun generate(data: GenerateRequest): ResponsePostGenerate {
        return try {
            aiServiceFeignClient.generate(data)
        } catch (e: feign.FeignException.BadRequest) {
            throw ApiException("Invalid request", 400)
        } catch (e: feign.FeignException) {
            throw ApiException("Service error: ${e.message}", e.status())
        } catch (e: Exception) {
            throw ApiException("Unexpected error", 500)
        }
    }

    fun getTask(taskId: UUID): ResponseGetTaskStatus {
        return try {
            aiServiceFeignClient.getTask(taskId)
        } catch (e: feign.FeignException.NotFound) {
            throw TaskNotFound()
        } catch (e: feign.FeignException) {
            ResponseGetTaskStatus(TaskStatus.FAILED, null)
        } catch (e: Exception) {
            throw ApiException("Unexpected error", 500)
        }
    }
}
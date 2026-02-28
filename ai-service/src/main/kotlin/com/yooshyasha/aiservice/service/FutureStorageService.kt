package com.yooshyasha.aiservice.service

import com.yooshyasha.aiservice.exceptions.TaskNotFound
import dto.GeneratedTasksResponse
import io.ktor.util.collections.*
import kotlinx.coroutines.Deferred
import org.springframework.stereotype.Service
import java.util.*

@Service
class FutureStorageService {
    private val taskMap: ConcurrentMap<UUID, Deferred<GeneratedTasksResponse>> = ConcurrentMap()

    fun save(taskId: UUID, data: Deferred<GeneratedTasksResponse>) {
        taskMap[taskId] = data
    }

    fun getTask(taskId: UUID): Deferred<GeneratedTasksResponse> {
        taskMap[taskId]?.let { return it }
        throw TaskNotFound()
    }

    fun remove(taskId: UUID) {
        @Suppress("DeferredResultUnused")
        taskMap.remove(taskId)
    }
}
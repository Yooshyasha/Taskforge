package com.yooshyasha.aiservice.storage

import exceptions.TaskNotFound
import dto.GeneratedTasksResponse
import io.ktor.util.collections.*
import kotlinx.coroutines.Deferred
import org.springframework.stereotype.Component
import java.util.*

@Component
class FutureStorage {
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
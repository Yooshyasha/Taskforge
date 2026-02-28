package com.yooshyasha.backend.storage

import com.yooshyasha.backend.dto.controller.RequestConfirmTasks
import com.yooshyasha.backend.enum.ConfirmedTaskStatus
import com.yooshyasha.backend.exceptions.GeneratedTasksNotFound
import com.yooshyasha.backend.exceptions.TasksZipFailed
import dto.GeneratedTasksResponse
import dto.TaskDTO
import io.ktor.util.collections.*
import org.springframework.stereotype.Component
import java.util.*

@Component
class GeneratedTasksStorage {
    private val storage: ConcurrentMap<UUID, GeneratedTasksResponse> = ConcurrentMap()

    fun save(taskId: UUID, data: GeneratedTasksResponse) {
        storage[taskId] = data
    }

    fun getTasks(taskId: UUID): GeneratedTasksResponse {
        storage[taskId]?.let { return it }
        throw GeneratedTasksNotFound()
    }

    fun remove(taskId: UUID) {
        storage.remove(taskId)
    }

    fun update(taskId: UUID, data: RequestConfirmTasks) {
        val oldData = getTasks(taskId)
        if (data.confirmTasks.size != oldData.tasks.size) throw TasksZipFailed()

        val pairs = oldData.tasks.zip(data.confirmTasks)

        val result = mutableListOf<TaskDTO>()
        pairs.onEach { pair ->
            when (pair.second.status) {
                ConfirmedTaskStatus.APPROVE -> result.add(pair.first)
                ConfirmedTaskStatus.UPDATE -> result.add(pair.second.taskDTO!!)
                ConfirmedTaskStatus.DELETE -> {}
            }
        }
        save(taskId, GeneratedTasksResponse(result.toList(), oldData.projectName))
    }
}
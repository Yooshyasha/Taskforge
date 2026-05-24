package dto

import enum.TaskControl
import kotlinx.serialization.Serializable

@Serializable
data class TaskDTO(
    val name: String,
    var description: String,
    val comments: List<String>?,
    val tags: List<String>,
    val vikunjaTaskId: Int?,
    val done: Boolean?,
) {
    val control: TaskControl
        get() {
            return if (vikunjaTaskId == null) {
                TaskControl.CREATE
            } else if (name.isEmpty()) {
                TaskControl.DELETE
            } else {
                TaskControl.EDIT
            }
        }
}

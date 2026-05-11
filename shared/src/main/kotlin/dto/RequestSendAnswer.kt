package dto

import java.util.*

data class RequestSendAnswer(
    val taskId: UUID,
    val answer: String,
)

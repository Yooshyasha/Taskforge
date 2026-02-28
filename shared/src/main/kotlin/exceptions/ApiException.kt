package exceptions

open class ApiException(override val message: String, val status: Int) : Exception(message)
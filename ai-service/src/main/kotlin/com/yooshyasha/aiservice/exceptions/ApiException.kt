package com.yooshyasha.aiservice.exceptions

open class ApiException(message: String, val status: Int) : Exception(message)
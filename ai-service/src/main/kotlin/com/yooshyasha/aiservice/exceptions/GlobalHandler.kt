package com.yooshyasha.aiservice.exceptions

import com.yooshyasha.aiservice.dto.controller.ResponseException
import jakarta.validation.ConstraintViolationException
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.web.HttpMediaTypeNotSupportedException
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.servlet.NoHandlerFoundException

@ControllerAdvice
class GlobalHandler {
    private val logger = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(ApiException::class)
    fun handleApiException(e: ApiException): ResponseEntity<ResponseException> {
        return ResponseEntity.status(e.status).body(ResponseException(e.message, e.status))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ResponseException> {
        val message = e.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseException(message, 400))
    }

    @ExceptionHandler(BindException::class)
    fun handleBind(e: BindException): ResponseEntity<ResponseException> {
        val message = e.bindingResult.fieldErrors.joinToString(", ") { "${it.field}: ${it.defaultMessage}" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseException(message, 400))
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleNotReadable(e: HttpMessageNotReadableException): ResponseEntity<ResponseException> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseException("Malformed request body", 400))
    }

    @ExceptionHandler(MissingServletRequestParameterException::class)
    fun handleMissingParam(e: MissingServletRequestParameterException): ResponseEntity<ResponseException> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ResponseException("Missing parameter: ${e.parameterName}", 400))
    }

    @ExceptionHandler(ConstraintViolationException::class)
    fun handleConstraintViolation(e: ConstraintViolationException): ResponseEntity<ResponseException> {
        val message = e.constraintViolations.joinToString(", ") { "${it.propertyPath}: ${it.message}" }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ResponseException(message, 400))
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleMethodNotAllowed(e: HttpRequestMethodNotSupportedException): ResponseEntity<ResponseException> {
        return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
            .body(ResponseException(e.message ?: "Method not allowed", 405))
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException::class)
    fun handleUnsupportedMediaType(e: HttpMediaTypeNotSupportedException): ResponseEntity<ResponseException> {
        return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
            .body(ResponseException(e.message ?: "Unsupported media type", 415))
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNotFound(e: NoHandlerFoundException): ResponseEntity<ResponseException> {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ResponseException("Endpoint not found", 404))
    }

    @ExceptionHandler(Throwable::class)
    fun handleUnexpected(e: Throwable): ResponseEntity<ResponseException> {
        logger.error("Unexpected error", e)
        return ResponseEntity.status(500).body(ResponseException("Unexpected error", 500))
    }
}
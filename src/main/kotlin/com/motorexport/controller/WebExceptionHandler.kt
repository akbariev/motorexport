package com.motorexport.controller

import java.util.function.Consumer
import org.slf4j.LoggerFactory
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.FieldError
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.bind.support.WebExchangeBindException


@RestControllerAdvice
class WebExceptionHandler {
    private val logger = LoggerFactory.getLogger(WebExceptionHandler::class.java)

    @ExceptionHandler(value = [(Exception::class)])
    fun internalServerError(ex: Exception): ResponseEntity<String> {
        logger.error("$ex, message is = ${ex.message}")
        return ResponseEntity("${ex.message}", HttpStatus.INTERNAL_SERVER_ERROR)
    }

    @ExceptionHandler(value = [(AccessDeniedException::class)])
    fun accessDeniedError(ex: AccessDeniedException): ResponseEntity<String> {
        return ResponseEntity(ex.message, HttpStatus.FORBIDDEN)
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException1(ex: MethodArgumentNotValidException): Map<String, String?>? {
        val errors: MutableMap<String, String?> = HashMap()
        ex.bindingResult.allErrors.forEach(Consumer { error: ObjectError ->
            val fieldName = (error as FieldError).field
            val errorMessage = error.getDefaultMessage()
            errors[fieldName] = errorMessage
        })
        return errors
    }

    @ExceptionHandler(WebExchangeBindException::class)
    fun handleBindException(ex: WebExchangeBindException): ResponseEntity<ErrorResponse> {
        val errors = ex.bindingResult.allErrors.map {
            when (it) {
                is FieldError -> "${it.field}: ${it.defaultMessage}"
                is ObjectError -> it.defaultMessage
                else -> "Unknown error"
            }
        }
        val errorMessage = "Validation failed: ${errors.joinToString(", ")}"
        val errorResponse = ErrorResponse(errorMessage)
        return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
    }
}

data class ErrorResponse(val message: String)

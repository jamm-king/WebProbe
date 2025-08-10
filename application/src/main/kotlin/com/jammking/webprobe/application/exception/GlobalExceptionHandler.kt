package com.jammking.webprobe.application.exception

import com.jammking.webprobe.common.exception.InvalidSearchRequestException
import com.jammking.webprobe.common.exception.WebProbeException
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(WebProbeException::class)
    fun handleWebProbeException(e: WebProbeException): ResponseEntity<String> {
        return when(e) {
            is InvalidSearchRequestException -> ResponseEntity.badRequest().body(e.message)
            else -> ResponseEntity.internalServerError().body("Internal error: ${e.message}")
        }
    }

    @ExceptionHandler(Exception::class)
    fun handleUnknown(e: Exception): ResponseEntity<String> =
        ResponseEntity.internalServerError().body("Unexpected error occurred.")
}
package pt.isel.liftdrop

import org.springframework.beans.TypeMismatchException
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler
import pt.isel.liftdrop.model.Problem
import pt.isel.pipeline.pt.isel.liftdrop.GlobalLogger

@ControllerAdvice
class CustomExceptionHandler : ResponseEntityExceptionHandler() {
    override fun handleMethodArgumentNotValid(
        ex: MethodArgumentNotValidException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        GlobalLogger.log("Http.CustomExceptionHandler ArgumentNotValid")
        return Problem.response(HttpStatus.BAD_REQUEST, Problem.InvalidRequestContent)
    }

    override fun handleTypeMismatch(
        ex: TypeMismatchException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        GlobalLogger.log("Http.CustomExceptionHandler TypeMismatch")
        return Problem.response(HttpStatus.BAD_REQUEST, Problem.InvalidRequestContent)
    }

    override fun handleHttpMessageNotReadable(
        ex: HttpMessageNotReadableException,
        headers: HttpHeaders,
        status: HttpStatusCode,
        request: WebRequest,
    ): ResponseEntity<Any>? {
        GlobalLogger.log("Http.CustomExceptionHandler NotReadable")
        return Problem.response(HttpStatus.BAD_REQUEST, Problem.InvalidRequestContent)
    }

    @ExceptionHandler(
        Exception::class,
    )
    fun handleAllExceptions(
        ex: Exception,
        request: WebRequest,
    ): ResponseEntity<String> {
        GlobalLogger.log("Http.CustomExceptionHandler AllExceptions: $ex")
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error")
    }
}

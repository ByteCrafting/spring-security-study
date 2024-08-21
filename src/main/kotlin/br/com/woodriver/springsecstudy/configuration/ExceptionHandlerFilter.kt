package br.com.woodriver.springsecstudy.configuration

import br.com.woodriver.springsecstudy.utils.objectToJson
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.HttpStatusCode
import org.springframework.security.oauth2.server.resource.InvalidBearerTokenException
import org.springframework.stereotype.Component
import org.springframework.web.filter.OncePerRequestFilter
import java.io.IOException

@Component
class ExceptionHandlerFilter : OncePerRequestFilter() {

    @Throws(ServletException::class, IOException::class)
    public override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain
    ) {
        try {
            filterChain.doFilter(request, response)
            if (response.status != 200)
                throw InvalidBearerTokenException("failed")
        } catch (e: InvalidBearerTokenException) {
            e.printStackTrace()
            setErrorResponse(
                status = HttpStatus.UNAUTHORIZED,
                isRetryable = false,
                message = "Invalid Bearer Token",
                messageDetails = e.message ?: "An unexpected error occurred",
                code = "SRV002",
                response = response,
                ex = e
            )
        } catch (e: RuntimeException) {
            e.printStackTrace()
            setErrorResponse(
                status = HttpStatus.INTERNAL_SERVER_ERROR,
                isRetryable = true,
                message = "Internal server error",
                messageDetails = e.message ?: "An unexpected error occurred",
                code = "SRV001",
                response = response,
                ex = e
            )
        }
    }

    private fun setErrorResponse(
        status: HttpStatusCode,
        code: String,
        isRetryable: Boolean,
        message: String,
        messageDetails: String,
        response: HttpServletResponse,
        ex: Throwable?
    ) {
        response.status = status.value()
        response.contentType = "application/json"
        // A class used for errors
        val apiError = ErrorMessage(
            code = code,
            isRetryable = isRetryable,
            message = message,
            details = ErrorMessage.ErrorDetails(
                cause = ex?.cause.toString(),
                description = messageDetails
            )
        )
        try {
            logger.error(apiError)
            response.writer.write(apiError.objectToJson())
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }
}

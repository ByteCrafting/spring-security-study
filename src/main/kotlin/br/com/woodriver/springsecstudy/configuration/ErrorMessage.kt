package br.com.woodriver.springsecstudy.configuration

data class ErrorMessage(
    val message: String,
    val code: String,
    val details: ErrorDetails,
    val isRetryable: Boolean,
) {
    data class ErrorDetails(
        val cause: String,
        val description: String
    )
}

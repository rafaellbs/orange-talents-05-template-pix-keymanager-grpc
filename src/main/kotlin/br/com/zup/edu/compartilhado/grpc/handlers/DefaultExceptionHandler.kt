package br.com.zup.edu.compartilhado.grpc.handlers

import br.com.zup.edu.compartilhado.grpc.ExceptionHandler
import io.grpc.Status

/**
 * pelo design, este classe nao deve ser gerenciada pelo Micronaut
 */
class DefaultExceptionHandler : ExceptionHandler<Exception> {
    override fun handle(e: Exception): ExceptionHandler.StatusWithDetails {
        val status = when (e) {
            is IllegalArgumentException -> Status.INVALID_ARGUMENT.withDescription(e.message)
            is IllegalStateException -> Status.FAILED_PRECONDITION.withDescription(e.message)
            else -> Status.UNKNOWN
        }

        return ExceptionHandler.StatusWithDetails(status.withCause(e))
    }

    override fun supports(e: Exception): Boolean {
        return true
    }
}
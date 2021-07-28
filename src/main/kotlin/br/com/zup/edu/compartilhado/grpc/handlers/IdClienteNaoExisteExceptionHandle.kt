package br.com.zup.edu.compartilhado.grpc.handlers

import br.com.zup.edu.compartilhado.grpc.ExceptionHandler
import br.com.zup.edu.compartilhado.grpc.exceptions.IdClienteNaoExisteException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class IdClienteNaoExisteExceptionHandle : ExceptionHandler<IdClienteNaoExisteException> {
    override fun handle(e: IdClienteNaoExisteException)
            : ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.NOT_FOUND
                .withDescription(e.message)
                .withCause(e)
        )
    }

    override fun supports(e: Exception): Boolean {
        return e is IdClienteNaoExisteException
    }
}
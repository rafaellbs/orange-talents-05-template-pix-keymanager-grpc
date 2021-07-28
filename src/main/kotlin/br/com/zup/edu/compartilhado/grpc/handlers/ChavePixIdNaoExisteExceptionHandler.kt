package br.com.zup.edu.compartilhado.grpc.handlers

import br.com.zup.edu.compartilhado.grpc.ErrorHandle
import br.com.zup.edu.compartilhado.grpc.ExceptionHandler
import br.com.zup.edu.compartilhado.grpc.exceptions.ChavePixIdNaoExisteException
import io.grpc.Status
import javax.inject.Singleton

@Singleton
class ChavePixIdNaoExisteExceptionHandler: ExceptionHandler<ChavePixIdNaoExisteException> {
    override fun handle(e: ChavePixIdNaoExisteException): ExceptionHandler.StatusWithDetails {
        return ExceptionHandler.StatusWithDetails(
            Status.NOT_FOUND
                .withDescription(e.message)
                .withCause(e)
        )

    }

    override fun supports(e: Exception): Boolean {
        return e is ChavePixIdNaoExisteException
    }

}
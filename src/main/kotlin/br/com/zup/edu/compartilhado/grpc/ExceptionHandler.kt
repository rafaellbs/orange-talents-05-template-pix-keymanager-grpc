package br.com.zup.edu.compartilhado.grpc

import io.grpc.Metadata
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto

interface ExceptionHandler<in E: Exception> {
    /**
     * Manipula exceções e mapeia eles para StatusWithDetails
     */

    fun handle(e: E): StatusWithDetails

    /**
     * Verifica se esta instância pode manipular um exceção específica ou não
     */

    fun supports(e: Exception): Boolean

    /**
     * Simples embulho para o Status e Metadata (trailers)
     */

    data class StatusWithDetails(val status: Status, val metadata: Metadata = Metadata()) {
        constructor(se: StatusRuntimeException): this(se.status, se.trailers ?: Metadata())
        constructor(sp: com.google.rpc.Status): this(StatusProto.toStatusRuntimeException(sp))

        fun asRuntimeException(): StatusRuntimeException {
            return status.asRuntimeException(metadata)
        }
    }
}
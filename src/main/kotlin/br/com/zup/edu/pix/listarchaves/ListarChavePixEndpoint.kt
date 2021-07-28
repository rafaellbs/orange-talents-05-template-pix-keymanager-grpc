package br.com.zup.edu.pix.listarchaves

import br.com.zup.edu.KeyManagerListaGrpcServiceGrpc
import br.com.zup.edu.ListarChavePixRequest
import br.com.zup.edu.ListarChavePixResponse
import br.com.zup.edu.TipoChave
import br.com.zup.edu.compartilhado.grpc.ErrorHandle
import br.com.zup.edu.repositories.ChavePixRepository
import com.google.protobuf.Timestamp
import io.grpc.stub.StreamObserver
import java.time.ZoneId
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandle
@Singleton
class ListarChavePixEndpoint(
    @Inject val chavePixRepository: ChavePixRepository,
) : KeyManagerListaGrpcServiceGrpc.KeyManagerListaGrpcServiceImplBase() {

    override fun listar(
        request: ListarChavePixRequest,
        responseObserver: StreamObserver<ListarChavePixResponse>
    ) {
        if (request.idCliente.isNullOrBlank()) {
            throw IllegalArgumentException("Id Cliente não pode ser nulo ou vazio")
        }

        val chaves = chavePixRepository.findAllByIdCliente(
            UUID.fromString(request.idCliente)
        ).map { chave ->
            ListarChavePixResponse.ChavePix.newBuilder()
                .setPixId(chave.pixId.toString())
                .setIdCliente(chave.idCliente.toString())
                .setTipo(TipoChave.valueOf(chave.tipo.name))
                .setChave(chave.chave)
                .setTipoConta(chave.tipoConta)
                .setCriadaEm(chave.criadoEm.let {
                    val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                    Timestamp.newBuilder()
                        .setNanos(createdAt.nano)
                        .setSeconds(createdAt.epochSecond)
                        .build()
                })
                .build()
        }

        responseObserver.onNext(
            ListarChavePixResponse.newBuilder()
                .addAllChaves(chaves)
                .build()
        )
        responseObserver.onCompleted()

    }
}
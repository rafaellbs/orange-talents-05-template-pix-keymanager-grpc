package br.com.zup.edu.pix.novachave

import br.com.zup.edu.KeyManagerRegistraGrpcServiceGrpc
import br.com.zup.edu.RegistraChavePixRequest
import br.com.zup.edu.RegistraChavePixResponse
import br.com.zup.edu.compartilhado.grpc.ErrorHandle
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton

@ErrorHandle
@Singleton
class CadastrarChaveEndpoint(
    @Inject val novaChavePixService: NovaChavePixService,
) : KeyManagerRegistraGrpcServiceGrpc.KeyManagerRegistraGrpcServiceImplBase() {

    override fun cadastrar(
        request: RegistraChavePixRequest,
        responseObserver: StreamObserver<RegistraChavePixResponse>?
    ) {
        val chavePixRequest = request.toModel()
        val chavePixCriada = novaChavePixService.registra(chavePixRequest)

        responseObserver?.onNext(
            RegistraChavePixResponse
                .newBuilder()
                .setIdCliente(chavePixCriada.idCliente.toString())
                .setIdPix(chavePixCriada.pixId.toString())
                .build()
        )
        responseObserver?.onCompleted()
    }
}




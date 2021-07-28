package br.com.zup.edu.pix.removerchave

import br.com.zup.edu.*
import br.com.zup.edu.compartilhado.grpc.ErrorHandle
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional

@ErrorHandle
@Singleton
class RemoverChavePixEndpoint(
    @Inject val removerChavePixService: RemoverChavePixService
): KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceImplBase()
{

    override fun remover(
        request: RemoveChavePixRequest,
        responseObserver: StreamObserver<RemoveChavePixResponse>?
    ) {

        removerChavePixService.deletar(request.idPix, request.idCliente)

        with(request) {
            responseObserver?.onNext(RemoveChavePixResponse.newBuilder()
                .setChave(idPix)
                .setIdCliente(idCliente)
                .build()
            )
        }
        responseObserver?.onCompleted()
    }
}
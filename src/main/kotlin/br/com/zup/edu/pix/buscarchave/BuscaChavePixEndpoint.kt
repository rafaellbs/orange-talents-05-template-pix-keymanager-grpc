package br.com.zup.edu.pix.buscarchave

import br.com.zup.edu.*
import br.com.zup.edu.compartilhado.grpc.ErrorHandle
import br.com.zup.edu.repositories.ChavePixRepository
import br.com.zup.edu.servicosexternos.SistemaPixBCBClient
import br.com.zup.edu.pix.buscarchave.parausuario.toModel
import io.grpc.stub.StreamObserver
import javax.inject.Inject
import javax.inject.Singleton
import javax.validation.Validator

@ErrorHandle
@Singleton
class BuscaChavePixEndpoint(
    @Inject val validator: Validator,
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val sistemaPixBCBClient: SistemaPixBCBClient
)
    : KeyManagerBuscaGrpcServiceGrpc.KeyManagerBuscaGrpcServiceImplBase()
{
    override fun buscar(
        request: BuscaChavePixRequest,
        responseObserver: StreamObserver<BuscaChavePixResponse>
    ) {
        val filtro = request.toModel(validator)
        val chaveInfo = filtro.filtra(
            repository = chavePixRepository,
            sistemaPixBCBClient = sistemaPixBCBClient
        )

        responseObserver.onNext(BuscaChavePixResponseConverte().converter(chaveInfo))
        responseObserver.onCompleted()
    }
}
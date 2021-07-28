package br.com.zup.edu.pix.removerchave

import br.com.zup.edu.compartilhado.anotacoescustomizadas.ValidUUID
import br.com.zup.edu.compartilhado.grpc.exceptions.ChavePixIdNaoExisteException
import br.com.zup.edu.repositories.ChavePixRepository
import br.com.zup.edu.servicosexternos.DeletePixKeyRequest
import br.com.zup.edu.servicosexternos.SistemaPixBCBClient
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.constraints.NotBlank

@Validated
@Singleton
class RemoverChavePixService(
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val sistemaPixBCBClient: SistemaPixBCBClient
) {

    @Transactional
    fun deletar(
        @NotBlank @ValidUUID(message = "pix ID com formato inválido") idPix: String?,
        @NotBlank @ValidUUID(message = "cliente ID com formato inválido") idCliente: String?
    ) {
        val uuidPixId = UUID.fromString(idPix)
        val uuidClienteId = UUID.fromString(idCliente)

        val chavePix = chavePixRepository.findByPixIdAndIdCliente(uuidPixId, uuidClienteId)
            .orElseThrow {
                ChavePixIdNaoExisteException("Chave pix não encontrada ou não pertence ao cliente")
            }

        chavePixRepository.delete(chavePix)

        val response = sistemaPixBCBClient.deletar(
            key = chavePix.chave,
            deletePixKeyRequest = DeletePixKeyRequest(
                key = chavePix.chave,
                participant = "60701190"
            )
        )

        if (response.status != HttpStatus.OK) {
            throw IllegalStateException("Erro ao remover chave Pix no Banco Central do Brasil.")
        }
    }
}

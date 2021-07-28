package br.com.zup.edu.pix.buscarchave

import br.com.zup.edu.compartilhado.anotacoescustomizadas.ValidUUID
import br.com.zup.edu.compartilhado.grpc.exceptions.ChavePixIdNaoExisteException
import br.com.zup.edu.repositories.ChavePixRepository
import br.com.zup.edu.servicosexternos.SistemaPixBCBClient
import io.micronaut.core.annotation.Introspected
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import org.slf4j.LoggerFactory
import java.util.*
import javax.inject.Singleton
import javax.validation.constraints.NotBlank
import javax.validation.constraints.Size

@Validated
@Singleton
sealed class Filtro {

    abstract fun filtra(repository: ChavePixRepository, sistemaPixBCBClient: SistemaPixBCBClient): ChavePixInfo

    @Introspected
    data class PorChavePix(
        @field:NotBlank @field:ValidUUID val idCliente: String,
        @field:NotBlank @field:ValidUUID val pixId: String
    ): Filtro() {

        fun idClienteParaUUID() = UUID.fromString(idCliente)
        fun pixIdParaUUID() = UUID.fromString(pixId)

        override fun filtra(repository: ChavePixRepository, sistemaPixBCBClient: SistemaPixBCBClient): ChavePixInfo {
            return repository.findByPixId(pixIdParaUUID())
                .filter { it.pertenceAo(idClienteParaUUID()) }
                .map(ChavePixInfo::of)
                .orElseThrow { ChavePixIdNaoExisteException("Chave Pix não encontrada") }
        }
    }

    @Introspected
    data class PorChave(@field:NotBlank @Size(max = 77) val chave: String): Filtro()
    {
        private val LOGGER = LoggerFactory.getLogger(this::class.java)

        override fun filtra(repository: ChavePixRepository, sistemaPixBCBClient: SistemaPixBCBClient): ChavePixInfo {
            return repository.findByChave(chave).map(ChavePixInfo::of).orElseGet {
                LOGGER.info("Consultando chave pix ${chave} no Banco Central do Brasil")

                val response = sistemaPixBCBClient.buscaPor(chave)
                println("cheguei aqui")
                when (response.status) {
                    HttpStatus.OK -> response.body()?.toModel()
                    null -> throw ChavePixIdNaoExisteException("Chave Pix Não encontrada")
                    else -> throw ChavePixIdNaoExisteException("Chave Pix Não encontrada")
                }
            }
        }
    }

    @Introspected
    class Invalido: Filtro() {
        override fun filtra(repository: ChavePixRepository, sistemaPixBCBClient: SistemaPixBCBClient): ChavePixInfo {
            return throw IllegalArgumentException("Chave Pix inválida ou não informada")
        }
    }
}

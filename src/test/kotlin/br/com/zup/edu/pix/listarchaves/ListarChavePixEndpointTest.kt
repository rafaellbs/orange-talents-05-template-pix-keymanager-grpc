package br.com.zup.edu.pix.listarchaves

import br.com.zup.edu.KeyManagerListaGrpcServiceGrpc
import br.com.zup.edu.ListarChavePixRequest
import br.com.zup.edu.TipoConta
import br.com.zup.edu.entities.ChavePix
import br.com.zup.edu.entities.ContaAssociada
import br.com.zup.edu.pix.novachave.TipoDeChave
import br.com.zup.edu.repositories.ChavePixRepository
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
class ListarChavePixEndpointTest(
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val clientGrpc: KeyManagerListaGrpcServiceGrpc.KeyManagerListaGrpcServiceBlockingStub
) {

    companion object {
        val IDCLIENTE = "c56dfef4-7901-44fb-84e2-a2cefb157890";
    }

    @BeforeEach
    fun setup() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `deve listar todas as chaves de um cliente a partir do idCliente`() {
        // cenario
        val chaveExistente1 = chavePixRepository.save(chavePix1())
        val chaveExistente2 = chavePixRepository.save(chavePix2())

        // acao
        val response = clientGrpc.listar(
            ListarChavePixRequest.newBuilder()
                .setIdCliente(IDCLIENTE)
                .build()
        )

        // validacao
        with(response) {
            assertEquals(2, chavesList.count())
            assertEquals(chaveExistente1.tipo.name, chavesList[0].tipo.name)
            assertEquals(chaveExistente1.chave, chavesList[0].chave)
            assertEquals(chaveExistente2.tipo.name, chavesList[1].tipo.name)
            assertEquals(chaveExistente2.chave, chavesList[1].chave)
        }
    }

    @Test
    fun `deve retornar uma lista vazia caso o cliente nao tenha chaves cadastradas`() {
        // acao
        val response = clientGrpc.listar(
            ListarChavePixRequest.newBuilder().setIdCliente(IDCLIENTE).build()
        )

        // validacao
        with(response) {
            assertTrue(chavesList.isEmpty())
        }
    }

    @Test
    fun `nao deve retornar uma lista com idCliente vazio`() {
        // acao
        val exception = assertThrows<StatusRuntimeException> {
            clientGrpc.listar(
                ListarChavePixRequest.newBuilder()
                    .setIdCliente("").build()
            )
        }

        // validacao
        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Id Cliente não pode ser nulo ou vazio", status.description)
        }
    }

    @Test
    fun `nao deve listar chaves com id Cliente com formato inválido`() {
        // acao
        val exception = assertThrows<StatusRuntimeException> {
            clientGrpc.listar(
                ListarChavePixRequest.newBuilder()
                    .setIdCliente("asasdede")
                    .build()
            )
        }

        //validacao
        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Invalid UUID string: asasdede", status.description)
        }
    }

    @Factory
    class Clients {
        @Singleton
        fun gRpcClient(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerListaGrpcServiceGrpc.KeyManagerListaGrpcServiceBlockingStub {
            return KeyManagerListaGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    fun chavePix1(): ChavePix {
        return ChavePix(
            idCliente = UUID.fromString(IDCLIENTE),
            tipo = TipoDeChave.CPF,
            chave = "08150283420",
            tipoConta = TipoConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                agencia = "3344",
                numeroDaConta = "567898",
                nomeDoTitular = "Rafael Lima",
                cpfDoTitular = "081.502.834-20",
                instituicao = "ITAÚ"
            )
        )
    }

    fun chavePix2(): ChavePix {
        return ChavePix(
            idCliente = UUID.fromString(IDCLIENTE),
            tipo = TipoDeChave.CELULAR,
            chave = "+08150283420",
            tipoConta = TipoConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                agencia = "3344",
                numeroDaConta = "567898",
                nomeDoTitular = "Rafael Lima",
                cpfDoTitular = "081.502.834-20",
                instituicao = "ITAÚ"
            )
        )
    }
}

package br.com.zup.edu.pix.buscarchave

import br.com.zup.edu.BuscaChavePixRequest
import br.com.zup.edu.KeyManagerBuscaGrpcServiceGrpc
import br.com.zup.edu.TipoConta
import br.com.zup.edu.entities.ChavePix
import br.com.zup.edu.entities.ContaAssociada
import br.com.zup.edu.pix.novachave.TipoDeChave
import br.com.zup.edu.repositories.ChavePixRepository
import br.com.zup.edu.servicosexternos.*
import io.grpc.ManagedChannel
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.micronaut.context.annotation.Factory
import io.micronaut.grpc.annotation.GrpcChannel
import io.micronaut.grpc.server.GrpcServerChannel
import io.micronaut.http.HttpResponse
import io.micronaut.test.annotation.MockBean
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.time.LocalDateTime
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
class BuscaChavePixEndpointTest(
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val sistemaPixBCBClient: SistemaPixBCBClient,
    @Inject val clientGrpc: KeyManagerBuscaGrpcServiceGrpc.KeyManagerBuscaGrpcServiceBlockingStub
) {

    @BeforeEach
    fun setup() {
        chavePixRepository.deleteAll()
    }

    @Test // correto
    fun `deve buscar uma chave pix com o idCliente e pixId`() {
        // cenario
        val chaveExistente = chavePixRepository.save(chavePix())

        // acao
        val response = clientGrpc.buscar(
            BuscaChavePixRequest.newBuilder()
                .setChavePix(
                    BuscaChavePixRequest.FiltroPorChavePix.newBuilder()
                        .setIdCliente(chaveExistente.idCliente.toString())
                        .setPixId(chaveExistente.pixId.toString())
                        .build()
                )
                .build()
        )

        // validacao
        with(response) {
            assertEquals(chaveExistente.pixId.toString(), pixId)
            assertEquals(chaveExistente.idCliente.toString(), idCliente)
            assertEquals(chaveExistente.tipo.name, chave.tipo.name)
            assertEquals(chaveExistente.chave, chave.chave)
            assertNotNull(this)
        }
    }

    @Test // correto
    fun `deve buscar uma chave pix pela chave que existe no banco`() {
        // cenario
        val chaveExistente = chavePixRepository.save(chavePix())

        // acao
        val response = clientGrpc.buscar(
            BuscaChavePixRequest.newBuilder()
                .setChave(chaveExistente.chave)
                .build()
        )

        // validacao
        with(response) {
            assertNotNull(this)
            assertEquals(chaveExistente.chave, chave.chave)
            assertEquals(chaveExistente.idCliente.toString(), idCliente)
            assertEquals(chaveExistente.pixId.toString(), pixId)
            assertEquals(chaveExistente.tipo.name, chave.tipo.name)
        }
    }

    @Test // correto
    fun `deve buscar uma chave pix que nao existe no banco, mas existe no BCB`() {
        // cenario

        Mockito.`when`(sistemaPixBCBClient.buscaPor(key = "08150283420")).thenReturn(
            HttpResponse.ok(chavePixDetalheResponse())
        )

        // acao
        val response = clientGrpc.buscar(BuscaChavePixRequest.newBuilder().setChave("08150283420").build())

        with(response) {
            assertNotNull(this)
            assertEquals("08150283420", chave.chave)
            assertEquals("", idCliente)
            assertEquals("", pixId)
        }
    }

    @Test //correto
    fun `nao deve buscar chave pix com dados idCliente e Pix Id inválidos`() {

        // acao
        val exception = assertThrows<StatusRuntimeException> {
            clientGrpc.buscar(
                BuscaChavePixRequest.newBuilder()
                    .setChavePix(
                        BuscaChavePixRequest.FiltroPorChavePix.newBuilder()
                            .setPixId("")
                            .setIdCliente("")
                            .build()
                    )
                    .build()
            )
        }

        // validacao
        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }
    }

    @Test // correto
    fun `nao deve buscar chave pix por pixId e idCliente quando registro nao existir`() {
        val pixIdNaoExistente = UUID.randomUUID()
        val idClienteNaoExistente = UUID.randomUUID()

        val exception = assertThrows<StatusRuntimeException> {
            clientGrpc.buscar(
                BuscaChavePixRequest.newBuilder()
                    .setChavePix(
                        BuscaChavePixRequest.FiltroPorChavePix.newBuilder()
                            .setIdCliente(idClienteNaoExistente.toString())
                            .setPixId(pixIdNaoExistente.toString())
                            .build()
                    )
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave Pix não encontrada", status.description)
        }

    }

    /*@Test
    fun `nao deve buscar uma chave pix quando nao existir no banco local e nem no BCB`(){
        // cenario
        *//*Mockito.`when`(sistemaPixBCBClient.buscaPor(key = "chavequennaoexiste"))
            .thenReturn(HttpResponse.notFound())*//*

        // acao
        val exception = assertThrows<StatusRuntimeException> {
            clientGrpc.buscar(BuscaChavePixRequest.newBuilder()
                .setChave("chavequenaoexiste")
                .build())
        }

        with(exception) {
            assertEquals(Status.NOT_FOUND.code, status.code)
        }
    }*/

    @Test // correto
    fun `nao deve buscar chave pix pela chave quando filtro invalido`() {
        // acao

        val exception = assertThrows<StatusRuntimeException> {
            clientGrpc.buscar(BuscaChavePixRequest.newBuilder().setChave("").build())
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }
    }

    @Test
    fun `nao deve buscar chave pix quando filtro invalido`() {
        val exception = assertThrows<StatusRuntimeException> {
            clientGrpc.buscar(BuscaChavePixRequest.newBuilder().build())
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Chave Pix inválida ou não informada", status.description)
        }
    }

    @MockBean(SistemaPixBCBClient::class)
    fun mockBancoCentral(): SistemaPixBCBClient? {
        return Mockito.mock(SistemaPixBCBClient::class.java)
    }

    @Factory
    class Clients {

        @Singleton
        fun clientGrpc(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerBuscaGrpcServiceGrpc.KeyManagerBuscaGrpcServiceBlockingStub {
            return KeyManagerBuscaGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}

fun chavePix(): ChavePix {
    return ChavePix(
        idCliente = UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"),
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

fun chavePixDetalheResponse(): ChavePixDetalheResponse {
    return ChavePixDetalheResponse(
        keyType = KeyType.CPF,
        key = "08150283420",
        bankAccount = BankAccount(
            participant = "60545500",
            branch = "4556",
            accountNumber = "555567",
            accountType = AccountType.CACC
        ),
        owner = Owner(TypeRequest.LEGAL_PERSON, name = "Rafael", taxIdNumber = "45755411840"),
        createdAt = LocalDateTime.now()
    )
}
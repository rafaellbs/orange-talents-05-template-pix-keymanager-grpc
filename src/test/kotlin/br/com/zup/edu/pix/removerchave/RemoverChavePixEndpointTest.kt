package br.com.zup.edu.pix.removerchave

import br.com.zup.edu.KeyManagerRemoveGrpcServiceGrpc
import br.com.zup.edu.RemoveChavePixRequest
import br.com.zup.edu.TipoConta
import br.com.zup.edu.entities.ChavePix
import br.com.zup.edu.entities.ContaAssociada
import br.com.zup.edu.pix.novachave.TipoDeChave
import br.com.zup.edu.repositories.ChavePixRepository
import br.com.zup.edu.servicosexternos.DeletePixKeyRequest
import br.com.zup.edu.servicosexternos.DeletePixKeyResponse
import br.com.zup.edu.servicosexternos.SistemaPixBCBClient
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
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
class RemoverChavePixEndpointTest {

    @field:Inject
    lateinit var chavePixRepository: ChavePixRepository

    @field:Inject
    lateinit var gRpcClients: KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceBlockingStub

    @field:Inject
    lateinit var sistemaPixBCBClient: SistemaPixBCBClient

    @BeforeEach
    fun setup() {
        chavePixRepository.deleteAll()
    }

    companion object {
        val CLIENTE_ID = UUID.randomUUID()
        val ID_PIX = UUID.randomUUID()
    }

    @Test
    fun `deve deletar uma chave pix com idPix e idCliente corretos`() {
        // cenario
        val chaveExistente = chavePixRepository.save(chavePix())

        Mockito.`when`(sistemaPixBCBClient.deletar(key = chaveExistente.chave,
        deletePixKeyRequest = DeletePixKeyRequest(
            key = chaveExistente.chave,
            participant = ContaAssociada.ITAU_UNIBANCO_ISPB
        )
        )).thenReturn(HttpResponse.ok(
            DeletePixKeyResponse(
            key = chaveExistente.chave,
            participant = ContaAssociada.ITAU_UNIBANCO_ISPB
        )
        ))

        // acao
        val response = gRpcClients.remover(
            RemoveChavePixRequest.newBuilder()
                .setIdPix(chaveExistente.pixId.toString())
                .setIdCliente(chaveExistente.idCliente.toString())
                .build()
        )

        // validacao
        with(response) {
            assertEquals(chaveExistente.idCliente.toString(), response.idCliente)
            assertEquals(chaveExistente.pixId.toString(), response.chave)
            assertEquals(0, chavePixRepository.count())
            assertEquals("26730629045", chaveExistente.chave)
        }
    }

    @Test
    fun `nao deve deletar chave pix quando nao for possivel deletar no BCB`() {
        // cenario
        val chaveExistente = chavePixRepository.save(chavePix())

        Mockito.`when`(sistemaPixBCBClient.deletar(key = chaveExistente.chave,
        deletePixKeyRequest = DeletePixKeyRequest(
            key = chaveExistente.chave,
            participant = ContaAssociada.ITAU_UNIBANCO_ISPB
        )
        )).thenReturn(HttpResponse.badRequest())

        val exception = assertThrows<StatusRuntimeException> {
            gRpcClients.remover(RemoveChavePixRequest.newBuilder()
                .setIdCliente(chaveExistente.idCliente.toString())
                .setIdPix(chaveExistente.pixId.toString())
                .build())
        }

        with(exception) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao remover chave Pix no Banco Central do Brasil.", status.description)
        }
    }

    @Test
    fun `nao deve remover chave pix com idPix que não existe`() {
        // cenario
        val chaveExistente = chavePixRepository.save(chavePix())

        // acao
        val exception = assertThrows<StatusRuntimeException> {
            gRpcClients.remover(
                RemoveChavePixRequest.newBuilder()
                    .setIdPix(ID_PIX.toString())
                    .setIdCliente(chaveExistente.idCliente.toString())
                    .build()
            )
        }

        // validacao
        with(exception) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não encontrada ou não pertence ao cliente", status.description)
        }
    }

    @Test
    fun `nao deve remover chave pix com o id cliente incorreto`() {
        // cenario
        val chaveExistente = chavePixRepository.save(chavePix())

        // acao
        val exception = assertThrows<StatusRuntimeException> {
            gRpcClients.remover(
                RemoveChavePixRequest.newBuilder()
                    .setIdPix(chaveExistente.pixId.toString())
                    .setIdCliente(UUID.randomUUID().toString())
                    .build()
            )
        }

        // validacao
        with(exception) {
            assertEquals(Status.NOT_FOUND.code, status.code)
            assertEquals("Chave pix não encontrada ou não pertence ao cliente", status.description)
        }
    }

    @Test
    fun `nao deve remover chave pix com dados invalidos`() {
        val exception = assertThrows<StatusRuntimeException> {
            gRpcClients.remover(
                RemoveChavePixRequest.newBuilder()
                    .setIdPix("ss44fff")
                    .setIdCliente("testando")
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }
    }

    @Test
    fun `nao deve remover chave pix com dados vazios`() {
        val exception = assertThrows<StatusRuntimeException> {
            gRpcClients.remover(
                RemoveChavePixRequest
                    .newBuilder()
                    .setIdPix("")
                    .setIdCliente("")
                    .build()
            )
        }

        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }
    }

    @Test
    fun `nao deve remover chave pix quando chave existente mas pertence a outro cliente`() {
        val chaveExistente = chavePixRepository.save(chavePix())

        val exception = assertThrows<StatusRuntimeException> {
            gRpcClients.remover(RemoveChavePixRequest.newBuilder()
                .setIdPix(chaveExistente.pixId.toString())
                .setIdCliente(UUID.randomUUID().toString())
                .build())
        }

        with(exception) {
            assertEquals(Status.NOT_FOUND.code, status.code)
        }
    }

    @Factory
    class Clients {
        @Singleton
        fun clientGrpc(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel): KeyManagerRemoveGrpcServiceGrpc.KeyManagerRemoveGrpcServiceBlockingStub {
            return KeyManagerRemoveGrpcServiceGrpc.newBlockingStub(channel)
        }
    }

    @MockBean(SistemaPixBCBClient::class)
    fun mockBCB(): SistemaPixBCBClient {
        return Mockito.mock(SistemaPixBCBClient::class.java)
    }

    fun chavePix(): ChavePix {
        return ChavePix(
            idCliente = CLIENTE_ID,
            tipo = TipoDeChave.CPF,
            chave = "26730629045",
            tipoConta = TipoConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                agencia = "0001",
                numeroDaConta = "3333",
                nomeDoTitular = "Rafael",
                cpfDoTitular = "26730629045",
                instituicao = "ITAU"
            )
        )
    }
}


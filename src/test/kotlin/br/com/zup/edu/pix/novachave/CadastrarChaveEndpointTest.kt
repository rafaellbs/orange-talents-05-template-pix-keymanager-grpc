package br.com.zup.edu.pix.novachave

import br.com.zup.edu.KeyManagerRegistraGrpcServiceGrpc
import br.com.zup.edu.RegistraChavePixRequest
import br.com.zup.edu.TipoConta
import br.com.zup.edu.entities.ChavePix
import br.com.zup.edu.entities.ContaAssociada
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
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.mockito.Mockito
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@MicronautTest(transactional = false)
class CadastrarChaveEndpointTest {

    @field:Inject
    lateinit var chavePixRepository: ChavePixRepository

    @field:Inject
    lateinit var clientGrpc: KeyManagerRegistraGrpcServiceGrpc.KeyManagerRegistraGrpcServiceBlockingStub

    @field:Inject
    lateinit var contasDeClientesNoItauClient: ContasDeClientesNoItauClient

    @field:Inject
    lateinit var sistemaPixBCBClient: SistemaPixBCBClient

    lateinit var registraChavePixRequest: RegistraChavePixRequest
    lateinit var dadosDaContaResponse: DadosDaContaResponse
    lateinit var chavePix: ChavePix

    @BeforeEach
    fun setup() {
        chavePixRepository.deleteAll()

        registraChavePixRequest = RegistraChavePixRequest.newBuilder()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157890")
            .setTipo(enumValueOf("CPF"))
            .setChave("45755411840")
            .setTipoConta(enumValueOf("CONTA_CORRENTE"))
            .build()

        dadosDaContaResponse = DadosDaContaResponse(
            tipo = "CONTA_CORRENTE",
            InstituicaoResponse(nome = "ITAÚ UNIBANCO S.A."),
            agencia = "0001",
            numero = "291900",
            titular = TitularResponse(nome = "Rafael M C Ponte", cpf = "02467781054")
        )

        chavePix = ChavePix(
            idCliente = UUID.fromString("c56dfef4-7901-44fb-84e2-a2cefb157890"),
            tipo = TipoDeChave.CPF,
            chave = "45755411840",
            tipoConta = TipoConta.CONTA_CORRENTE,
            conta = ContaAssociada(
                agencia = "0001",
                numeroDaConta = "291900",
                nomeDoTitular = "Rafael M C Ponte",
                cpfDoTitular = "02467781054",
                instituicao = "ITAÚ UNIBANCO S.A."
            )
        )
    }

    @AfterEach
    fun cleanUp() {
        chavePixRepository.deleteAll()
    }

    @Test
    fun `Deve cadastrar uma nova chave pix`() {
        // cenario
        Mockito.`when`(
            contasDeClientesNoItauClient.buscarContaPorTipo(
                id = registraChavePixRequest.idCliente,
                tipo = registraChavePixRequest.tipoConta
            )
        ).thenReturn(HttpResponse.ok(dadosDaContaResponse))

        Mockito.`when`(
            sistemaPixBCBClient.registra(createPixKeyRequest(chavePix))
        ).thenReturn(HttpResponse.created(createPixKeyResponse(chavePix)))

        // acao
        val response = clientGrpc.cadastrar(registraChavePixRequest)

        // validacao
        with(response) {
            assertEquals(registraChavePixRequest.idCliente, idCliente)
            assertTrue(idPix.matches("^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[89ab][0-9a-f]{3}-[0-9a-f]{12}$".toRegex()))
            assertNotNull(idPix)
            assertTrue(chavePixRepository.existsByChave("45755411840"))
        }
    }

    @Test
    fun `nao deve registrar chave pix quando nao for possivel registrar chave BCB`() {
        // cenario
        Mockito.`when`(
            contasDeClientesNoItauClient.buscarContaPorTipo(
                id = registraChavePixRequest.idCliente,
                tipo = registraChavePixRequest.tipoConta
            )
        ).thenReturn(HttpResponse.ok(dadosDaContaResponse))

        Mockito.`when`(
            sistemaPixBCBClient.registra(createPixKeyRequest(chavePix))
        ).thenReturn(HttpResponse.badRequest())
        // acao

        val exception = assertThrows<StatusRuntimeException> {
            clientGrpc.cadastrar(registraChavePixRequest)
        }

        // validacao
        with(exception) {
            assertEquals(Status.FAILED_PRECONDITION.code, status.code)
            assertEquals("Erro ao cadastrar chave pix no Banco Central do Brasil", status.description)
        }

    }

    @Test
    fun `nao deve cadastrar uma chave que ja existe no banco`() {
        // cenario
        chavePixRepository.save(chavePix)

        // acao
        val exception = assertThrows<StatusRuntimeException> {
            clientGrpc.cadastrar(registraChavePixRequest)
        }

        // validacao
        with(exception) {
            assertEquals(Status.ALREADY_EXISTS.code, status.code)
            assertEquals("Chave Pix '${registraChavePixRequest.chave}' existente.", status.description)
            assertEquals(1, chavePixRepository.count())
        }
    }

    @Test
    fun `nao deve cadastrar chave pix com id de cliente que nao existe`() {
        val registraChavePixRequestAlterada = registraChavePixRequest.newBuilderForType()
            .setIdCliente("c56dfef4-7901-44fb-84e2-a2cefb157999")
            .setTipo(enumValueOf("CPF"))
            .setChave("45755411840")
            .setTipoConta(enumValueOf("CONTA_CORRENTE"))
            .build()

        Mockito.`when`(
            contasDeClientesNoItauClient.buscarContaPorTipo(
                registraChavePixRequestAlterada.idCliente,
                registraChavePixRequestAlterada.tipoConta
            )
        ).thenReturn(HttpResponse.notFound())

        Mockito.`when`(sistemaPixBCBClient.registra(createPixKeyRequest(chavePix)))
            .thenReturn(HttpResponse.notFound(createPixKeyResponse(chavePix)))

        val exception = assertThrows<StatusRuntimeException> {
            clientGrpc.cadastrar(registraChavePixRequestAlterada)
        }

        assertEquals(Status.FAILED_PRECONDITION.code, exception.status.code)
        assertEquals("Cliente não existente!", exception.status.description)
    }

    @Test
    fun `nao deve cadastrar chave pix com campos vazios`() {
        // acao
        val exception = assertThrows<StatusRuntimeException> {
            clientGrpc.cadastrar(RegistraChavePixRequest.newBuilder().build())
        }

        // validacao
        with(exception) {
            assertEquals(Status.INVALID_ARGUMENT.code, status.code)
            assertEquals("Dados inválidos", status.description)
        }
    }

    @MockBean(ContasDeClientesNoItauClient::class)
    fun mockandoContasDeClientesNoItauClient(): ContasDeClientesNoItauClient {
        return Mockito.mock(ContasDeClientesNoItauClient::class.java)
    }

    @MockBean(SistemaPixBCBClient::class)
    fun mockBCB(): SistemaPixBCBClient {
        return Mockito.mock(SistemaPixBCBClient::class.java)
    }

    @Factory
    class Clients {
        @Singleton
        fun criarStubClient(@GrpcChannel(GrpcServerChannel.NAME) channel: ManagedChannel):
                KeyManagerRegistraGrpcServiceGrpc.KeyManagerRegistraGrpcServiceBlockingStub {
            return KeyManagerRegistraGrpcServiceGrpc.newBlockingStub(channel)
        }
    }
}

fun createPixKeyRequest(chavePix: ChavePix): CreatePixKeyRequest {
    return CreatePixKeyRequest(
        keyType = KeyType.CPF,
        key = chavePix.chave,
        bankAccount = BankAccount(
            participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
            branch = chavePix.conta.agencia,
            accountNumber = chavePix.conta.numeroDaConta,
            accountType = AccountType.CACC
        ),
        owner = Owner(
            type = TypeRequest.NATURAL_PERSON,
            name = chavePix.conta.nomeDoTitular,
            taxIdNumber = chavePix.conta.cpfDoTitular
        )
    )
}

fun createPixKeyResponse(chavePix: ChavePix): CreatePixKeyResponse {
    return CreatePixKeyResponse(
        keyType = KeyType.CPF,
        key = chavePix.chave,
        bankAccount = BankAccount(
            participant = ContaAssociada.ITAU_UNIBANCO_ISPB,
            branch = chavePix.conta.agencia,
            accountNumber = chavePix.conta.numeroDaConta,
            accountType = AccountType.CACC
        ),
        owner = Owner(
            type = TypeRequest.NATURAL_PERSON,
            name = chavePix.conta.nomeDoTitular,
            taxIdNumber = chavePix.conta.cpfDoTitular
        ),
        createdAt = ""
    )
}
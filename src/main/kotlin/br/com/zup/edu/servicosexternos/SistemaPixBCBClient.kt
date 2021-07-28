package br.com.zup.edu.servicosexternos

import br.com.zup.edu.TipoConta
import br.com.zup.edu.pix.buscarchave.ChavePixInfo
import br.com.zup.edu.entities.ContaAssociada
import br.com.zup.edu.pix.novachave.TipoDeChave
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.*
import io.micronaut.http.client.annotation.Client
import java.time.LocalDateTime

@Client(value = "\${bcb.pix.url}")
interface SistemaPixBCBClient {

    @Post(value = "/api/v1/pix/keys")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    fun registra(@Body chavePixBCBRequest: CreatePixKeyRequest): HttpResponse<CreatePixKeyResponse>

    @Delete("/api/v1/pix/keys/{key}")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    fun deletar(@PathVariable key: String, @Body deletePixKeyRequest: DeletePixKeyRequest):
            HttpResponse<DeletePixKeyResponse>

    @Get("/api/v1/pix/keys/{key}")
    @Produces(MediaType.APPLICATION_XML)
    @Consumes(MediaType.APPLICATION_XML)
    fun buscaPor(@PathVariable key: String): HttpResponse<ChavePixDetalheResponse>
}

data class CreatePixKeyRequest(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
)

enum class KeyType {
    CPF, RANDOM, EMAIL, CNPJ, PHONE
}

data class BankAccount(
    val participant: String,
    val branch: String,
    val accountNumber: String,
    val accountType: AccountType,
)

enum class AccountType {
    CACC, SVGS, UNKNOWN_ACCOUNT
}

data class Owner(
    val type: TypeRequest,
    val name: String,
    val taxIdNumber: String
)

enum class TypeRequest {
    NATURAL_PERSON, LEGAL_PERSON
}

data class CreatePixKeyResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: String
) {}

data class DeletePixKeyRequest(
    val key: String,
    val participant: String = ContaAssociada.ITAU_UNIBANCO_ISPB,
)

data class DeletePixKeyResponse(
    val key: String,
    val participant: String
)

data class ChavePixDetalheResponse(
    val keyType: KeyType,
    val key: String,
    val bankAccount: BankAccount,
    val owner: Owner,
    val createdAt: LocalDateTime
) {

    fun toModel(): ChavePixInfo {
        return ChavePixInfo(
            tipo = TipoDeChave.valueOf(keyType.name),
            chave = key,
            tipoDeConta = when (bankAccount.accountType) {
                AccountType.CACC -> TipoConta.CONTA_CORRENTE
                AccountType.SVGS -> TipoConta.CONTA_POUPANCA
                AccountType.UNKNOWN_ACCOUNT -> TipoConta.UNKNOWN_TIPO_CONTA
            },
            conta = ContaAssociada(
                agencia = bankAccount.branch,
                numeroDaConta = bankAccount.accountNumber,
                nomeDoTitular = owner.name,
                cpfDoTitular = owner.taxIdNumber,
                instituicao = "ITAU",
            ),
            criadaEm = createdAt
        )
    }
}
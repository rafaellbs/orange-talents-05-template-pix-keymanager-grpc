package br.com.zup.edu.pix.novachave

import br.com.zup.edu.TipoConta
import br.com.zup.edu.compartilhado.grpc.exceptions.ChavePixExistenteException
import br.com.zup.edu.entities.ChavePix
import br.com.zup.edu.repositories.ChavePixRepository
import br.com.zup.edu.servicosexternos.*
import io.micronaut.http.HttpStatus
import io.micronaut.validation.Validated
import javax.inject.Inject
import javax.inject.Singleton
import javax.transaction.Transactional
import javax.validation.Valid

@Validated
@Singleton
class NovaChavePixService(
    @Inject val chavePixRepository: ChavePixRepository,
    @Inject val itauClient: ContasDeClientesNoItauClient,
    @Inject val sistemaPixBCBClient: SistemaPixBCBClient
) {

    @Transactional
    fun registra(@Valid novaChavePix: ChavePixRequest): ChavePix {
        if (chavePixRepository.existsByChave(novaChavePix.chave!!)) {
            throw ChavePixExistenteException("Chave Pix '${novaChavePix.chave}' existente.")
        }

        val response = itauClient.buscarContaPorTipo(novaChavePix.idCliente!!, novaChavePix.tipoConta!!)
        val conta = response.body()?.toModel() ?: throw IllegalStateException("Cliente não existente!")

        val chavePix = novaChavePix.toModel(conta)
        chavePixRepository.save(chavePix)

        val responseBCB = sistemaPixBCBClient.registra(createPixKeyRequest(chavePix))
        if (responseBCB.status != HttpStatus.CREATED) {
            throw IllegalStateException("Erro ao cadastrar chave pix no Banco Central do Brasil")
        }

        chavePix.atualiza(responseBCB.body()!!.key)
        chavePix.valida = true

        return chavePix
    }
}

fun createPixKeyRequest(chavePix: ChavePix): CreatePixKeyRequest {
    return CreatePixKeyRequest(
        keyType = when (chavePix.tipo) {
            TipoDeChave.CPF -> KeyType.CPF
            TipoDeChave.CELULAR -> KeyType.PHONE
            TipoDeChave.ALEATORIA -> KeyType.RANDOM
            TipoDeChave.EMAIL -> KeyType.EMAIL
        },
        key = chavePix.chave,
        bankAccount = BankAccount(
            participant = "60701190",
            branch = chavePix.conta.agencia,
            accountNumber = chavePix.conta.numeroDaConta,
            accountType = when (chavePix.tipoConta) {
                TipoConta.CONTA_CORRENTE -> AccountType.CACC
                TipoConta.CONTA_POUPANCA -> AccountType.SVGS
                else -> AccountType.UNKNOWN_ACCOUNT
            }
        ),
        owner = Owner(
            type = TypeRequest.NATURAL_PERSON,
            name = chavePix.conta.nomeDoTitular,
            taxIdNumber = chavePix.conta.cpfDoTitular
        )
    )
}
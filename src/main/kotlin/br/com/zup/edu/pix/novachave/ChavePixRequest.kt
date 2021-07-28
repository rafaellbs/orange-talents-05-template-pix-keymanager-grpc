package br.com.zup.edu.pix.novachave

import br.com.zup.edu.TipoConta
import br.com.zup.edu.compartilhado.anotacoescustomizadas.ValidPixKey
import br.com.zup.edu.compartilhado.anotacoescustomizadas.ValidUUID
import br.com.zup.edu.entities.ChavePix
import br.com.zup.edu.entities.ContaAssociada
import io.micronaut.core.annotation.Introspected
import java.util.*
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull
import javax.validation.constraints.Size

@ValidPixKey
@Introspected
data class ChavePixRequest(
    @field:ValidUUID
    @field:NotBlank
    val idCliente: String?,

    @field:NotNull
    val tipo: TipoDeChave?,

    @field:Size(max = 77)
    val chave: String?,

    @field:NotNull
    val tipoConta: TipoConta?
) {

    fun toModel(conta: ContaAssociada): ChavePix {
        return ChavePix(
            idCliente = UUID.fromString(idCliente),
            tipo = TipoDeChave.valueOf(tipo!!.name),
            chave = /*if (this.tipo == TipoDeChave.ALEATORIA) UUID.randomUUID().toString() else*/ chave!!,
            tipoConta = TipoConta.valueOf(tipoConta!!.name),
            conta = conta
        )
    }
}
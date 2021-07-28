package br.com.zup.edu.entities

import io.micronaut.core.annotation.Introspected
import javax.persistence.Column
import javax.persistence.Embeddable
import javax.validation.constraints.NotBlank

@Embeddable
@Introspected
class ContaAssociada(

    @field:NotBlank
    @field:Column(nullable = false)
    val agencia: String,

    @field:NotBlank
    @field:Column(nullable = false)
    val numeroDaConta: String,

    @field:NotBlank
    @field:Column(nullable = false)
    val nomeDoTitular: String,

    @field:NotBlank
    @field:Column(nullable = false)
    val cpfDoTitular: String,

    @field:NotBlank
    @field:Column(nullable = false)
    val instituicao: String
) {

    companion object {
        public val ITAU_UNIBANCO_ISPB: String = "60701190"
    }
}

package br.com.zup.edu.pix.novachave

import br.com.zup.edu.entities.ContaAssociada

data class DadosDaContaResponse(
    val tipo: String,
    val instituicao: InstituicaoResponse,
    val agencia: String,
    val numero: String,
    val titular: TitularResponse
) {
    fun toModel(): ContaAssociada {
        return ContaAssociada(
            instituicao = this.instituicao.nome,
            agencia = this.agencia,
            numeroDaConta = this.numero,
            nomeDoTitular = this.titular.nome,
            cpfDoTitular = this.titular.cpf
        )
    }
}

data class TitularResponse(val nome: String, val cpf: String)
data class InstituicaoResponse(val nome: String)

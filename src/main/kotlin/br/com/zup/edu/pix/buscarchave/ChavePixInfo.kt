package br.com.zup.edu.pix.buscarchave

import br.com.zup.edu.TipoConta
import br.com.zup.edu.entities.ChavePix
import br.com.zup.edu.entities.ContaAssociada
import br.com.zup.edu.pix.novachave.TipoDeChave
import java.time.LocalDateTime
import java.util.*

data class ChavePixInfo(
    val idCliente: UUID? = null,
    val pixId: UUID? = null,
    val tipo: TipoDeChave,
    val chave: String,
    val tipoDeConta: TipoConta,
    val conta: ContaAssociada,
    val criadaEm: LocalDateTime
) {
        companion object {
        fun of(chavePix: ChavePix): ChavePixInfo {
            return ChavePixInfo(
                idCliente = chavePix.idCliente,
                pixId = chavePix.pixId,
                tipo = chavePix.tipo,
                chave = chavePix.chave,
                tipoDeConta = chavePix.tipoConta,
                conta = chavePix.conta,
                criadaEm = chavePix.criadoEm
            )
        }
    }
}

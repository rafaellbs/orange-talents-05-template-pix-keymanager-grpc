package br.com.zup.edu.pix.novachave

import br.com.zup.edu.RegistraChavePixRequest
import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import br.com.zup.edu.pix.novachave.TipoDeChave
fun RegistraChavePixRequest.toModel(): ChavePixRequest {
    return ChavePixRequest(
        idCliente = idCliente,
        tipo = when (tipo) {
            TipoChave.UNKNOWN_TIPO_CHAVE -> null
            else -> TipoDeChave.valueOf(tipo.name)
        },
        chave = chave,
        tipoConta = when (tipoConta) {
            TipoConta.UNKNOWN_TIPO_CONTA -> null
            else -> TipoConta.valueOf(tipoConta.name)
        }
    )
}
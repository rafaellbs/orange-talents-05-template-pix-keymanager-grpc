package br.com.zup.edu.pix.buscarchave

import br.com.zup.edu.BuscaChavePixRequest
import br.com.zup.edu.BuscaChavePixResponse
import br.com.zup.edu.TipoChave
import br.com.zup.edu.TipoConta
import com.google.protobuf.Timestamp
import java.time.ZoneId

class BuscaChavePixResponseConverte {
    fun converter(chaveInfo: ChavePixInfo): BuscaChavePixResponse {
        return BuscaChavePixResponse.newBuilder()
            .setIdCliente(chaveInfo.idCliente?.toString() ?: "")
            .setPixId(chaveInfo.pixId?.toString() ?: "")
            .setChave(
                BuscaChavePixResponse.ChavePix.newBuilder()
                    .setTipo(TipoChave.valueOf(chaveInfo.tipo.name))
                    .setChave(chaveInfo.chave)
                    .setConta(
                        BuscaChavePixResponse.ChavePix.ContaInfo.newBuilder()
                            .setTipo(TipoConta.valueOf(chaveInfo.tipoDeConta.name))
                            .setInstituicao(chaveInfo.conta.instituicao)
                            .setNomeDoTitular(chaveInfo.conta.nomeDoTitular)
                            .setCpfDoTitular(chaveInfo.conta.cpfDoTitular)
                            .setAgencia(chaveInfo.conta.agencia)
                            .setNumeroDaConta(chaveInfo.conta.numeroDaConta)
                            .build()
                    )
                    .setCriadaEm(chaveInfo.criadaEm.let {
                        val createdAt = it.atZone(ZoneId.of("UTC")).toInstant()
                        Timestamp.newBuilder()
                            .setSeconds(createdAt.epochSecond)
                            .setNanos(createdAt.nano)
                            .build()
                    })
            ).build()
    }
}

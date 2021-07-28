package br.com.zup.edu.servicosexternos

import br.com.zup.edu.TipoConta
import br.com.zup.edu.pix.novachave.DadosDaContaResponse
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.PathVariable
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.annotation.Client

@Client("http://localhost:9091/api/v1")
interface ContasDeClientesNoItauClient {

    @Get("/clientes/{id}/contas{?tipo}")
    fun buscarContaPorTipo(@PathVariable id: String, @QueryValue tipo: TipoConta): HttpResponse<DadosDaContaResponse>
}


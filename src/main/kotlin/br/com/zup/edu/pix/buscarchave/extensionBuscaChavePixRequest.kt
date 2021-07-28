package br.com.zup.edu.pix.buscarchave.parausuario

import br.com.zup.edu.BuscaChavePixRequest
import br.com.zup.edu.BuscaChavePixRequest.FiltroCase.*
import br.com.zup.edu.pix.buscarchave.Filtro
import javax.validation.ConstraintViolationException
import javax.validation.Validator

fun BuscaChavePixRequest.toModel(validator: Validator): Filtro {
    val filtro = when(filtroCase) {
        CHAVEPIX ->  Filtro.PorChavePix(idCliente = chavePix.idCliente, pixId = chavePix.pixId)
        CHAVE -> Filtro.PorChave(chave)
        FILTRO_NOT_SET -> Filtro.Invalido()
    }

    val violations = validator.validate(filtro)
    if (violations.isNotEmpty()) {
        throw ConstraintViolationException(violations)
    }

    return filtro
}
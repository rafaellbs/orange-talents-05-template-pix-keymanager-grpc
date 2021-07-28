package br.com.zup.edu.repositories

import br.com.zup.edu.entities.ChavePix
import io.micronaut.data.annotation.Repository
import io.micronaut.data.jpa.repository.JpaRepository
import java.util.*

@Repository
interface ChavePixRepository : JpaRepository<ChavePix, Long> {
    fun existsByChave(chave: String): Boolean

    fun existsByIdCliente(idCliente: UUID): Boolean

    fun findByPixIdAndIdCliente(idPix: UUID, idCliente: UUID): Optional<ChavePix>

    fun findByPixId(pixId: UUID): Optional<ChavePix>

    fun findByChave(chave: String): Optional<ChavePix>

    fun findAllByIdCliente(idCliente: UUID): List<ChavePix>
}
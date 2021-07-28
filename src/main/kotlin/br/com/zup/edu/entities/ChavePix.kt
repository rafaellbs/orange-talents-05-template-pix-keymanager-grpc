package br.com.zup.edu.entities

import br.com.zup.edu.TipoConta
import br.com.zup.edu.pix.novachave.TipoDeChave
import java.time.LocalDateTime
import java.util.*
import javax.persistence.*
import javax.validation.Valid
import javax.validation.constraints.NotBlank
import javax.validation.constraints.NotNull

@Entity
@Table(name = "chaves_pix_clientes")
class ChavePix(
    @field:NotNull
    @field:Column(nullable = false)
    val idCliente: UUID,

    @field:NotNull
    @field:Enumerated(EnumType.STRING)
    @field:Column(nullable = false)
    val tipo: TipoDeChave,

//    @field:NotBlank
    @field:Column(nullable = false, unique = true)
    var chave: String,

    @field:NotNull
    @field:Enumerated(EnumType.STRING)
    val tipoConta: TipoConta,

    @field:Valid
    @Embedded
    val conta: ContaAssociada
) {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null

    @field:Column(length = 16, nullable = false)
    val pixId: UUID = UUID.randomUUID()

    @Column(nullable = false)
    val criadoEm: LocalDateTime = LocalDateTime.now()

    @Column(nullable = false)
    var valida: Boolean = false

    val ehValida: Boolean get() = valida

    fun pertenceAo(idCliente: UUID): Boolean {
        return this.idCliente == idCliente
    }

    fun atualiza(chave: String) {
        this.chave = chave
    }

    override fun toString(): String {
        return "ChavePix(idCliente=$idCliente, tipoChave=$tipo, valorChave='$chave', tipoConta=$tipoConta, id=$id, criadoEm=$criadoEm)"
    }
}
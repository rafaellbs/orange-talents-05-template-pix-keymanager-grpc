package br.com.zup.edu.pix.novachave

import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class TipoDeChaveTest {

    @Nested
    inner class ALEATORIA {

        @Test
        fun `deve ser valido quando chave aleatoria for nula ou vazia`() {
            with(TipoDeChave.ALEATORIA) {
                assertTrue(valida(""))
                assertTrue(valida(null))
            }
        }

        @Test
        fun `nao deve ser valido se a chave aleatoria possuir um valor`() {
            with(TipoDeChave.ALEATORIA) {
                assertFalse(valida("chave"))
            }
        }
    }

    @Nested
    inner class EMAIL {

        @Test
        fun `deve ser valido quando passado um email corretamente`() {
            with(TipoDeChave.EMAIL) {
                assertTrue(valida("email@qualquer.com"))
            }
        }

        @Test
        fun `nao deve ser valido quando passado um email num formato invalido`() {
            with(TipoDeChave.EMAIL) {
                assertFalse(valida("formatoinvalido"))
            }
        }

        @Test
        fun `nao deve ser valido quando o valor for nulo ou vazio`() {
            with(TipoDeChave.EMAIL) {
                assertFalse(valida(""))
                assertFalse(valida(null))
            }
        }
    }

    @Nested
    inner class CELULAR {
        @Test
        fun `deve ser valido quando a chave estiver no formato de celular`(){
            with(TipoDeChave.CELULAR){
                assertTrue(valida("+5511956564343"))
            }
        }

        @Test
        fun `nao deve ser valido quando a chave nao estiver no formato de celular`() {
            with(TipoDeChave.CELULAR) {
                assertFalse(valida("22222222222"))
                assertFalse(valida("+12aa3223"))
                assertFalse(valida("11956564343"))
            }
        }

        @Test
        fun `nao deve ser valido quando a chave for nula ou vazia`() {
            with(TipoDeChave.CELULAR) {
                assertFalse(valida(""))
                assertFalse(valida(null))
            }
        }
    }

    @Nested
    inner class CPF {
        @Test
        fun `deve ser valido se o cpf for valido`() {
            with(TipoDeChave.CPF) {
                assertTrue(valida("26730629045"))
            }
        }

        @Test
        fun `nao deve ser valido com a chave cpf invalido ou conter letras`() {
            with(TipoDeChave.CPF){
                assertFalse(valida("111.111.111-11"))
                assertFalse(valida("12345678912"))
                assertFalse(valida("abfcdaassassa"))
            }
        }

        @Test
        fun `nao deve ser valido com chave de cpf nula ou vazia`() {
            with(TipoDeChave.CPF) {
                assertFalse(valida(""))
                assertFalse(valida(""))
            }
        }

        @Test
        fun `nao deve ser valido caso a chave tenha mais que 11 digitos`() {
            with(TipoDeChave.CPF) {
                assertFalse(valida("2673062904526730629045"))
            }
        }
    }
}
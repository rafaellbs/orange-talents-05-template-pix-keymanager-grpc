package br.com.zup.edu.compartilhado.anotacoescustomizadas

import br.com.zup.edu.pix.novachave.ChavePixRequest
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.validation.validator.constraints.ConstraintValidator
import io.micronaut.validation.validator.constraints.ConstraintValidatorContext
import javax.inject.Singleton
import javax.validation.Constraint
import javax.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Target(AnnotationTarget.CLASS, AnnotationTarget.TYPE)
@Retention(AnnotationRetention.RUNTIME)
@Constraint(validatedBy = [ValidPixKeyValidator::class])
annotation class ValidPixKey(
    val message: String = "chave Pix inválida (\${validatedValue.tipo})",
    val groups: Array<KClass<Any>> = [],
    val payload: Array<KClass<Payload>> = []
)

@Singleton
class ValidPixKeyValidator : ConstraintValidator<ValidPixKey, ChavePixRequest> {
    override fun isValid(
        value: ChavePixRequest?,
        annotationMetadata: AnnotationValue<ValidPixKey>,
        context: ConstraintValidatorContext
    ): Boolean {
        if (value?.tipo == null) {
            return false
        }
        return value.tipo.valida(value.chave)
    }
}

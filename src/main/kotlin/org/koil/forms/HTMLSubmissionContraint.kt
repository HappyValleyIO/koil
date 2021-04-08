package org.koil.forms

import org.jsoup.Jsoup
import org.jsoup.safety.Whitelist
import javax.validation.Constraint
import javax.validation.ConstraintValidator
import javax.validation.ConstraintValidatorContext
import javax.validation.Payload
import kotlin.reflect.KClass

@MustBeDocumented
@Constraint(validatedBy = [HTMLSubmissionValidator::class])
@Target(AnnotationTarget.FUNCTION, AnnotationTarget.FIELD, AnnotationTarget.PROPERTY_GETTER)
@Retention(AnnotationRetention.RUNTIME)
annotation class HTMLSubmissionConstraint(
    val message: String = "Looks like you're trying to pass some HTML that's not allowed!",
    val groups: Array<KClass<*>> = [],
    val payload: Array<KClass<out Payload>> = []
)

class HTMLSubmissionValidator : ConstraintValidator<HTMLSubmissionConstraint?, String?> {
    override fun initialize(contactNumber: HTMLSubmissionConstraint?) {}
    override fun isValid(
        html: String?,
        cxt: ConstraintValidatorContext
    ): Boolean {
        return Jsoup.isValid(html, Whitelist.relaxed().addTags("strike", "s"))
    }
}

package xyz.fmcy.validation;

import org.intellij.lang.annotations.Language;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.expression.BeanFactoryResolver;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;

import javax.annotation.Resource;
import javax.validation.Constraint;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Documented
@Constraint(validatedBy = SpELValidator.class)
public @interface SpELValid {
    String message() default "";
    @Language("SpEL")
    String value();

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};

}



class SpELValidator implements ConstraintValidator<SpELValid, Object> {
    @Resource
    private ApplicationContext applicationContext;

    private String validEL;

    @Bean
    public SpelExpressionParser spelExpressionParser() {
        return new SpelExpressionParser();
    }

    @Override
    public void initialize(SpELValid valid) {
        this.validEL = valid.value();
        ConstraintValidator.super.initialize(valid);
    }

    @Override
    public boolean isValid(Object o, ConstraintValidatorContext context) {
        StandardEvaluationContext elContext = new StandardEvaluationContext();
        elContext.setBeanResolver(new BeanFactoryResolver(applicationContext));
        elContext.setVariable("data", o);
        Object value = spelExpressionParser().parseExpression(validEL).getValue(elContext);
        if (value instanceof Boolean bool) {
            return bool;
        } else return false;
    }
}
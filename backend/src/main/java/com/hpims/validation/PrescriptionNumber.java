package com.hpims.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 处方号格式验证注解
 * 格式：RX + 8位日期(yyyyMMdd) + 3位序号
 * 示例：RX20250101001
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = PrescriptionNumberValidator.class)
@Documented
public @interface PrescriptionNumber {
    String message() default "处方号格式不正确，应为：RX + 8位日期(yyyyMMdd) + 3位序号，如：RX20250101001";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}


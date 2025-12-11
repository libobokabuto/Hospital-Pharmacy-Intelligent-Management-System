package com.hpims.validation;

import com.hpims.util.ValidationUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * 处方号格式验证器
 */
public class PrescriptionNumberValidator implements ConstraintValidator<PrescriptionNumber, String> {

    @Override
    public void initialize(PrescriptionNumber constraintAnnotation) {
        // 初始化方法，可以在这里获取注解参数
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        // 如果值为空，由@NotBlank等注解处理
        if (value == null || value.trim().isEmpty()) {
            return true;
        }
        // 使用ValidationUtil验证处方号格式
        return ValidationUtil.isValidPrescriptionNumber(value);
    }
}


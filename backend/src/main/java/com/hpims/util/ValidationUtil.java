package com.hpims.util;

import java.util.regex.Pattern;

/**
 * 验证工具类
 * 提供常用的数据验证方法
 */
public class ValidationUtil {

    /**
     * 手机号正则表达式
     */
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");

    /**
     * 邮箱正则表达式
     */
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");

    /**
     * 身份证号正则表达式（18位）
     */
    private static final Pattern ID_CARD_PATTERN = Pattern.compile("^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[0-9Xx]$");

    /**
     * 处方号正则表达式（格式：RX + 日期 + 序号，如：RX20250101001）
     */
    private static final Pattern PRESCRIPTION_NUMBER_PATTERN = Pattern.compile("^RX\\d{8}\\d{3}$");

    /**
     * 验证手机号
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 验证邮箱
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 验证身份证号
     */
    public static boolean isValidIdCard(String idCard) {
        if (idCard == null || idCard.trim().isEmpty()) {
            return false;
        }
        return ID_CARD_PATTERN.matcher(idCard).matches();
    }

    /**
     * 验证处方号格式
     * 格式：RX + 8位日期(yyyyMMdd) + 3位序号
     * 示例：RX20250101001
     */
    public static boolean isValidPrescriptionNumber(String prescriptionNumber) {
        if (prescriptionNumber == null || prescriptionNumber.trim().isEmpty()) {
            return false;
        }
        return PRESCRIPTION_NUMBER_PATTERN.matcher(prescriptionNumber).matches();
    }

    /**
     * 验证字符串是否为空（null、空字符串、仅空白字符）
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * 验证字符串是否不为空
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * 验证字符串长度是否在范围内
     */
    public static boolean isLengthInRange(String str, int min, int max) {
        if (str == null) {
            return min <= 0;
        }
        int length = str.length();
        return length >= min && length <= max;
    }

    /**
     * 验证数字是否在范围内
     */
    public static boolean isNumberInRange(Number number, Number min, Number max) {
        if (number == null) {
            return false;
        }
        double value = number.doubleValue();
        double minValue = min != null ? min.doubleValue() : Double.MIN_VALUE;
        double maxValue = max != null ? max.doubleValue() : Double.MAX_VALUE;
        return value >= minValue && value <= maxValue;
    }

    /**
     * 验证是否为有效的年龄（0-150）
     */
    public static boolean isValidAge(Integer age) {
        if (age == null) {
            return false;
        }
        return age >= 0 && age <= 150;
    }

    /**
     * 验证是否为有效的性别
     */
    public static boolean isValidGender(String gender) {
        if (gender == null || gender.trim().isEmpty()) {
            return false;
        }
        String normalized = gender.trim().toLowerCase();
        return "男".equals(normalized) || "女".equals(normalized) 
            || "male".equals(normalized) || "female".equals(normalized)
            || "M".equals(normalized) || "F".equals(normalized);
    }
}


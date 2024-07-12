package com.xiaofan.usercenter.utils;

import java.util.regex.Pattern;

/**
 * 密码校验器。
 */
public class PasswordValidatorUtils {

    /**
     * 密码由四种元素组成（数字、大写字母、小写字母、特殊字符），且必须包含全部四种元素；密码长度大于等于8个字符。
     */
    public static boolean isValid(String password) {
        // 正则表达式的内容如下:
        // ^(?![0-9A-Za-z]+$)(?![0-9A-Z\W]+$)(?![0-9a-z\W]+$)(?![A-Za-z\W]+$)[0-9A-Za-z~!@#$%^&*()__+`\-={}|[\]\\:";'<>?,./]{8,}$
        // 在 Java 中使用，需要转义；转义后的结果如下。
        String pattern = "^(?![0-9A-Za-z]+$)(?![0-9A-Z\\W]+$)(?![0-9a-z\\W]+$)(?![A-Za-z\\W]+$)[0-9A-Za-z~!@#$%^&*()_+`\\-={}|\\[\\]\\\\:\";'<>?,./]{8,}$";
        return Pattern.matches(pattern, password);
    }

}


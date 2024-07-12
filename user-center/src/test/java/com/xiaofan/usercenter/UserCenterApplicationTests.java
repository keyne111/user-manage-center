package com.xiaofan.usercenter;
import com.baomidou.mybatisplus.core.toolkit.Assert;

import com.xiaofan.usercenter.utils.PasswordValidatorUtils;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;

@SpringBootTest
public class UserCenterApplicationTests {

    @Test
    void test2(){
        isValid("");
        isValid("中文");
        isValid("123456789");
        isValid("aaabbbccc");
        isValid("AAABBBCCCabc");
        isValid("AAAbbb123");
        isValid("abcABC1@中文");
        isValid("aB1@");

        isValid("abcABC1@");
        isValid("aaaBBB111@");
        isValid("aAs!1111");
    }

    private static void isValid(String text) {
        System.out.println(text + " === " + PasswordValidatorUtils.isValid(text));
    }


}
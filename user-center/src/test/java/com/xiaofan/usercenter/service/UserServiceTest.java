package com.xiaofan.usercenter.service;

import com.xiaofan.usercenter.model.domain.User;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 用户服务测试
 *
 * @author xiaofan
 */
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    public void testAddUser(){
        User user = new User();
        // user.setId(0L);
        user.setUsername("xiaofan");
        user.setUserAccount("123");
        user.setAvatarUrl("https://ts4.cn.mm.bing.net/th?id=ORMS.7cb7d655628049ee2c8d90a4dee9ee66&pid=Wdp&w=612&h=328&qlt=90&c=1&rs=1&dpr=1.4800000190734863&p=0");
        user.setGender(0);
        user.setUserPassword("123456");
        user.setPhone("123");
        user.setEmail("456");

        boolean result = userService.save(user);
        System.out.println(user.getId());
        Assertions.assertEquals(true,result);
    }

    @Test
    void userRegister() {
        String userAccount="xiaofan";
        String userPassword="";
        String checkPassword="12345678";
        String planetCode="1";
        long result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode );
        Assertions.assertEquals(-1,result);

        userAccount="xia";
        userPassword="12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode );
        Assertions.assertEquals(-1,result);

        checkPassword="123456";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode );
        Assertions.assertEquals(-1,result);

        userAccount="xiao❤fa";
        checkPassword="12345678";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode );
        Assertions.assertEquals(-1,result);

        userAccount="1234";
        userPassword="Aa+12345";
        checkPassword="Aa+12345";
        result = userService.userRegister(userAccount, userPassword, checkPassword,planetCode );
        Assertions.assertEquals(-1,result);
        //
        // userAccount="xiaofan2";
        // planetCode="1";
        // result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        // Assertions.assertEquals(-1,result);

        userAccount="xiaofan2";
        planetCode="1";
        result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        Assertions.assertEquals(-1,result);
    }
}
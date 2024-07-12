package com.xiaofan.usercenter.controller;

import com.xiaofan.usercenter.constants.UserConstant;
import com.xiaofan.usercenter.model.domain.User;
import com.xiaofan.usercenter.model.domain.dto.UserLoginDto;
import com.xiaofan.usercenter.model.domain.dto.UserRegisterDto;
import com.xiaofan.usercenter.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/user")
@Tag(name = "用户管理")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Long userRegister(@RequestBody UserRegisterDto registerDto){
        log.info("用户注册:{}",registerDto);
        if(registerDto==null){
            return null;
        }
        String userAccount = registerDto.getUserAccount();
        String userPassword = registerDto.getUserPassword();
        String checkPassword = registerDto.getCheckPassword();
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            return null;
        }

        return userService.userRegister(userAccount, userPassword, checkPassword);

    }


    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public User userLogin(@RequestBody UserLoginDto loginDto, HttpServletRequest request){
        log.info("用户登录:{}",loginDto);
        if(loginDto==null){
            return null;
        }
        String userAccount = loginDto.getUserAccount();
        String userPassword = loginDto.getUserPassword();
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            return null;
        }

        return userService.userLogin(userAccount, userPassword, request);
    }

    @GetMapping("/search/{username}")
    @Operation(summary = "根据用户名查询")
    public List<User> userSearch(@PathVariable String username,HttpServletRequest request){
        //鉴权，管理员才有权限
        if (!isAdmin(request)){
            return new ArrayList<>();
        }

        log.info("根据用户名查询：{}",username);
        //因为不管有没有传入用户名，都要查询，所以就不写校验为null了
        List<User> userList = userService.searchByUserName(username);

        //对用户进行脱敏操作
       return userList.stream().map(originUser -> {
           return userService.getSafetyUser(originUser);
       }).collect(Collectors.toList());


    }

    @PostMapping("/delete")
    @Operation(summary = "管理员删除用户")
    public Boolean delete(@RequestParam long userId,HttpServletRequest request){
        //鉴权，管理员才有权限
        if (!isAdmin(request)) {
            return false;
        }
        if(userId<=0){
            return false;
        }
        // 因为yml开启了逻辑删除，所以会把他修改isDelete字段
        return userService.removeById(userId);

    }

    /**
     * 是否管理员
     * @param request
     * @return
     */
    private boolean isAdmin(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        User user = (User) userObj;
        if (user == null || user.getUserRole() != UserConstant.ADMIN_ROLE) {
            return false;
        }
        return true;
    }

}

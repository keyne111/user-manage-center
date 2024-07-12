package com.xiaofan.usercenter.controller;

import com.xiaofan.usercenter.common.ErrorCode;
import com.xiaofan.usercenter.common.Result;
import com.xiaofan.usercenter.constants.UserConstant;
import com.xiaofan.usercenter.exception.BusinessException;
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
@RequestMapping("/user")
@Tag(name = "用户管理")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Long> userRegister(@RequestBody UserRegisterDto registerDto){
        log.info("用户注册:{}",registerDto);
        if(registerDto==null){
             throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        String userAccount = registerDto.getUserAccount();
        String userPassword = registerDto.getUserPassword();
        String checkPassword = registerDto.getCheckPassword();
        String planetCode = registerDto.getPlanetCode();

        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode)){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        long id = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return Result.success(id);
    }


    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<User> userLogin(@RequestBody UserLoginDto loginDto, HttpServletRequest request){
        log.info("用户登录:{}",loginDto);
        if(loginDto==null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        String userAccount = loginDto.getUserAccount();
        String userPassword = loginDto.getUserPassword();
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }

        User user = userService.userLogin(userAccount, userPassword, request);
        return Result.success(user);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户注销")
    public Result<String> userLogout(HttpServletRequest request){
       if(request==null){
           throw new BusinessException(ErrorCode.PARAM_ERROR);
       }
        userService.userLogout(request);
        return Result.success("ok");
    }


    @GetMapping("/search")
    @Operation(summary = "根据用户名查询")
    public Result<List<User>> userSearch( String username,HttpServletRequest request){
        //鉴权，管理员才有权限
        if (!isAdmin(request)){
            throw new BusinessException(ErrorCode.NO_AUTH);

        }

        log.info("根据用户名查询：{}",username);
        //因为不管有没有传入用户名，都要查询，所以就不写校验为null了
        List<User> userList = userService.searchByUserName(username);

        //对用户进行脱敏操作
        List<User> list = userList.stream().map(originUser -> {
            return userService.getSafetyUser(originUser);
        }).collect(Collectors.toList());
        return Result.success(list);


    }

    @PostMapping("/delete")
    @Operation(summary = "管理员删除用户")
    public Result<Boolean> delete(@RequestParam long userId,HttpServletRequest request){
        //鉴权，管理员才有权限
        if (!isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if(userId<=0){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        // 因为yml开启了逻辑删除，所以会把他修改isDelete字段
        boolean result = userService.removeById(userId);
        return Result.success(result);

    }

    @GetMapping("/current")
    @Operation(summary = "得到当前用户具体信息")
    public Result<User> getCurrentUser(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(UserConstant.USER_LOGIN_STATUS);
        User user=(User) userObj;
        if(user == null){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        Long id = user.getId();
        User currentUser = userService.getById(id);
        return Result.success(currentUser);


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

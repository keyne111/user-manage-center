package com.xiaofan.usercenter.controller;

import com.alibaba.fastjson2.JSON;
import com.xiaofan.usercenter.common.Const;
import com.xiaofan.usercenter.common.ErrorCode;
import com.xiaofan.usercenter.common.Result;
import com.xiaofan.usercenter.constants.UserConstant;
import com.xiaofan.usercenter.exception.BusinessException;
import com.xiaofan.usercenter.model.domain.User;
import com.xiaofan.usercenter.model.domain.dto.UserLoginDto;
import com.xiaofan.usercenter.model.domain.dto.UserRegisterDto;
import com.xiaofan.usercenter.service.UserService;

import com.xiaofan.usercenter.utils.CookieUtil;
import com.xiaofan.usercenter.utils.JsonUtil;
import com.xiaofan.usercenter.utils.RedisUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/user")
@Tag(name = "用户管理")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;


    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private RedisTemplate redisTemplate;

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
    public Result<User> userLogin(@RequestBody UserLoginDto loginDto, HttpServletRequest request, HttpSession session, HttpServletResponse response){
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

        //存储到redis中
        redisUtil.set(session.getId(), JsonUtil.obj2String(user), Const.REDIS_SESSION_EXPIRE);
        //生成cookie
        CookieUtil.writeLoginToken(session.getId(),response);

        return Result.success(user);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户注销")
    public Result<String> userLogout(HttpServletRequest request,HttpServletResponse response){
        //读取sessionID
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isBlank(loginToken)){
            throw new BusinessException(ErrorCode.NOT_ALOGIN,"不可注销");
        }

        // userService.userLogout(request);
        //删除session
        CookieUtil.deleteLoginToken(request,response);
        //删除缓存
        redisUtil.del(loginToken);
        return Result.success("ok");
    }

    @GetMapping("/current")
    @Operation(summary = "得到当前用户具体信息")
    public Result<User> getCurrentUser(HttpServletRequest request){
        String loginToken = CookieUtil.readLoginToken(request);
        if(StringUtils.isBlank(loginToken)){
            throw new BusinessException(ErrorCode.NOT_ALOGIN);
        }
        //从Redis中获取用户的json数据
        String userJson = (String)redisUtil.get(loginToken);
        //json转换成Use对象
        User cookieUser = JsonUtil.string2Obj(userJson, User.class);

        if(cookieUser == null){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        return Result.success(cookieUser);


    }

    @GetMapping("/search")
    @Operation(summary = "根据用户名查询")
    public Result<List<User>> userSearchByAdmin( String username,HttpServletRequest request){
        // 做了个AOP自动鉴权了
        // if (!isAdmin(request)){
        //     throw new BusinessException(ErrorCode.NO_AUTH);
        // }

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
    public Result<Boolean> deleteByAdmin(@RequestParam long userId,HttpServletRequest request){
        //  做了个AOP自动鉴权了
        // if (!isAdmin(request)) {
        //     throw new BusinessException(ErrorCode.NO_AUTH);
        // }
        if(userId<=0){
            throw new BusinessException(ErrorCode.PARAM_ERROR);
        }
        // 因为yml开启了逻辑删除，所以会把他修改isDelete字段
        boolean result = userService.removeById(userId);
        return Result.success(result);

    }



    /**
     * 是否管理员  初始鉴权，现在优化成AOP鉴权了
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

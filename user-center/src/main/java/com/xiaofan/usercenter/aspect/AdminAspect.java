package com.xiaofan.usercenter.aspect;

import com.alibaba.fastjson2.JSON;
import com.xiaofan.usercenter.common.ErrorCode;
import com.xiaofan.usercenter.constants.UserConstant;
import com.xiaofan.usercenter.exception.BusinessException;
import com.xiaofan.usercenter.model.domain.User;
import com.xiaofan.usercenter.utils.CookieUtil;
import com.xiaofan.usercenter.utils.JsonUtil;
import com.xiaofan.usercenter.utils.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


/**
 * 鉴权的切面，扫描UserController的 Admin结尾的方法，证明需要鉴权
 */
@Aspect
@Component
public class AdminAspect {

    @Autowired
    private RedisUtil redisUtil;

    Logger logger = LoggerFactory.getLogger(AdminAspect.class);
    @Pointcut("execution(* com.xiaofan.usercenter.controller.UserController+.*Admin(..)) && within(com.xiaofan.usercenter.controller.*)")
    public void executeService(){

    }


    @Before("executeService()")
    public void doBeforeAdvice(JoinPoint joinPoint) {
        logger.info("==> json方法调用开始...");
        //获取目标方法的参数信息
        Object[] obj = joinPoint.getArgs();
        //AOP代理类的信息
        joinPoint.getThis();
        //代理的目标对象
        joinPoint.getTarget();
        //用的最多 通知的签名
        Signature signature = joinPoint.getSignature();
        //代理的是哪一个方法
        logger.info("==> 代理的是哪一个方法 :" + signature.getName());
        //AOP代理类的名字
        logger.info("==> AOP代理类的名字:" + signature.getDeclaringTypeName());
        //AOP代理类的类（class）信息
        signature.getDeclaringType();
        //获取RequestAttributes
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        //从获取RequestAttributes中获取HttpServletRequest的信息
        HttpServletRequest request = (HttpServletRequest) requestAttributes.resolveReference(RequestAttributes.REFERENCE_REQUEST);
        logger.info("==> 请求者的IP：" + request.getRemoteAddr());
        //如果要获取Session信息的话，可以这样写：
        // HttpSession session = (HttpSession) requestAttributes.resolveReference(RequestAttributes.REFERENCE_SESSION);
        // Object userObj = session.getAttribute(UserConstant.USER_LOGIN_STATUS);
        // User user = (User) userObj;
        // if(user == null){
        //     throw new BusinessException(ErrorCode.NOT_ALOGIN);
        // }
        // if (user.getUserRole() != UserConstant.ADMIN_ROLE) {
        //     throw new BusinessException(ErrorCode.NO_AUTH);
        // }
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
        if(cookieUser.getUserRole() != UserConstant.ADMIN_ROLE){
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
    }
}

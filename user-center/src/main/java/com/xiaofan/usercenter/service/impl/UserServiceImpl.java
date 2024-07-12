package com.xiaofan.usercenter.service.impl;
import java.time.LocalDateTime;

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaofan.usercenter.constants.UserConstant;
import com.xiaofan.usercenter.model.domain.User;
import com.xiaofan.usercenter.service.UserService;
import com.xiaofan.usercenter.mapper.UserMapper;
import com.xiaofan.usercenter.utils.MD5Util;
import com.xiaofan.usercenter.utils.PasswordValidatorUtils;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
* @author xiaofan
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2024-07-10 19:05:25
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService{



    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 确认密码
     * @return 用户id
     */
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 字段非空
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            return -1;
        }
        // 账号长度要>=4位
        if(userAccount.length()<4){
            return -1;
        }
        // 密码和确认密码的长度都要>=8位
        if(userPassword.length()<8 || checkPassword.length()<8){
            return -1;
        }

        // 账号包含特殊字符则放回-1
        String validPattern = "[`~ !@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]"; //合法表达式
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            return -1;
        }

        // 密码和校验密码相同
        if(!userPassword.equals(checkPassword)){
            return -1;
        }
        //密码强校验：密码由四种元素组成（数字、大写字母、小写字母、特殊字符），且必须包含全部四种元素；密码长度大于等于8个字符。
        if(!PasswordValidatorUtils.isValid(userPassword)){
            return -1;
        }

        // 账号不能重复
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        User user = this.getOne(queryWrapper);
        // 查到了用户，说明了重复
        if(user !=null){
            return -1;
        }

        // 加盐加密
        String salt = MD5Util.getSalt();
        String encryptedPassword = MD5Util.getSaltMD5(userPassword, salt);

        //插入数据
        User user1=new User();
        user1.setUserAccount(userAccount);
        user1.setSalt(salt);
        user1.setUserPassword(encryptedPassword);
        boolean saveResult = this.save(user1);
        if(!saveResult){
            return -1;
        }
        return user1.getId();
    }


    /**
     * 用户登录
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 字段非空
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            return null;
        }
        // 账号长度要>=4位
        if(userAccount.length()<4){
            return null;
        }
        // 密码和确认密码的长度都要>=8位
        if(userPassword.length()<8){
            return null;
        }

        // 账号包含特殊字符则放回-1
        String validPattern = "[`~ !@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]"; //合法表达式
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            return null;
        }

        //密码强校验：密码由四种元素组成（数字、大写字母、小写字母、特殊字符），且必须包含全部四种元素；密码长度大于等于8个字符。
        if(!PasswordValidatorUtils.isValid(userPassword)){
            return null;
        }


        // 比对输入的账号和密码
        QueryWrapper<User> queryWrapper=new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        User user = this.getOne(queryWrapper);
        // 查不到用户
        if(user ==null){
            log.info("userInfo select fail");
            return null;
        }
        String encryptedPassword = MD5Util.getSaltMD5(userPassword, user.getSalt());
        queryWrapper.eq("userPassword",encryptedPassword);
        user = this.getOne(queryWrapper);
        // 密码不匹配
        if(user==null){
            log.info("user login fail,userAccount cannot match userPassword");
            return null;
        }

        //脱敏后的用户信息
        User safeUser = getSafetyUser(user);

        request.getSession().setAttribute(UserConstant.USER_LOGIN_STATUS,safeUser);

        return safeUser;
    }

    /**
     * 得到脱敏后的用户
     * @param originUser
     * @return
     */
    public User getSafetyUser(User originUser) {
        User safeUser=new User();
        safeUser.setId(originUser.getId());
        safeUser.setUsername(originUser.getUsername());
        safeUser.setUserAccount(originUser.getUserAccount());
        safeUser.setAvatarUrl(originUser.getAvatarUrl());
        safeUser.setGender(originUser.getGender());
        safeUser.setSalt("");
        safeUser.setUserPassword("");
        safeUser.setPhone(originUser.getPhone());
        safeUser.setEmail(originUser.getEmail());
        safeUser.setUserRole(originUser.getUserRole());
        safeUser.setUserStatus(originUser.getUserStatus());
        safeUser.setCreateTime(originUser.getCreateTime());
        safeUser.setUpdateTime(originUser.getUpdateTime());
        return safeUser;
    }


    /**
     * 根据用户名查询
     * @param username 用户名
     * @return
     */
    public List<User> searchByUserName(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNotBlank(username)){
            queryWrapper.like("username",username);
        }
        //用户名为空时，默认查询全部用户,否则，根据用户名查询
        return this.list(queryWrapper);

    }
}





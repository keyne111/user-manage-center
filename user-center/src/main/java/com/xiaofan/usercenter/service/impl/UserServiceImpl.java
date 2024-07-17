package com.xiaofan.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xiaofan.usercenter.common.ErrorCode;
import com.xiaofan.usercenter.constants.UserConstant;
import com.xiaofan.usercenter.exception.BusinessException;
import com.xiaofan.usercenter.model.domain.User;
import com.xiaofan.usercenter.service.UserService;
import com.xiaofan.usercenter.mapper.UserMapper;
import com.xiaofan.usercenter.utils.JsonUtil;
import com.xiaofan.usercenter.utils.MD5Util;
import com.xiaofan.usercenter.utils.PasswordValidatorUtils;
import com.xiaofan.usercenter.utils.RedisUtil;
import io.swagger.v3.core.util.Json;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author xiaofan
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2024-07-10 19:05:25
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Autowired
    private RedisUtil redisUtil;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     * 用户注册
     *
     * @param userAccount   用户账号
     * @param userPassword  用户密码
     * @param checkPassword 确认密码
     * @param planetCode
     * @return 用户id
     */
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        // 字段非空
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "字段不能为空");
        }
        // 账号长度要>=4位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号长度过短");
        }
        // 密码和确认密码的长度都要>=8位
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码长度过短");
        }
        // 长度过长，非法
        if (planetCode.length() > 5) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "星球编号异常");
        }

        // 账号包含特殊字符则放回-1
        String validPattern = "[`~ !@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]"; //合法表达式
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号包含特殊字符");
        }

        // 密码和校验密码不相同
        if (!userPassword.equals(checkPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码和校验密码不相同");
        }
        //密码强校验：密码由四种元素组成（数字、大写字母、小写字母、特殊字符），且必须包含全部四种元素；密码长度大于等于8个字符。
        if (!PasswordValidatorUtils.isValid(userPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码由四种元素组成（数字、大写字母、小写字母、特殊字符），且必须包含全部四种元素；密码长度大于等于8个字符");
        }

        // 账号不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        User user = this.getOne(queryWrapper);
        // 查到了用户，说明了重复
        if (user != null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号重复");
        }

        // 星球编号不能重复
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode", planetCode);
        user = this.getOne(queryWrapper);
        // 查到了用户，说明了重复
        if (user != null) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "星球编号重复");
        }

        // 加盐加密
        String salt = MD5Util.getSalt();
        String encryptedPassword = MD5Util.getSaltMD5(userPassword, salt);

        //插入数据
        User user1 = new User();
        user1.setUserAccount(userAccount);
        user1.setSalt(salt);
        user1.setUserPassword(encryptedPassword);
        user1.setPlanetCode(planetCode);
        boolean saveResult = this.save(user1);
        if (!saveResult) {
            throw new BusinessException(ErrorCode.SAVE_ERROR);
        }
        return user1.getId();
    }


    /**
     * 用户登录
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @param request
     * @return 脱敏后的用户信息
     */
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 字段非空
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "字段不能为空");
        }
        // 账号长度要>=4位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号长度过短");
        }
        // 密码和确认密码的长度都要>=8位
        if (userPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码长度过短");
        }

        // 账号包含特殊字符则放回-1
        String validPattern = "[`~ !@#$%^&*()+=|{}':;',\\\\[\\\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]"; //合法表达式
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "账号包含特殊字符");
        }

        //密码强校验：密码由四种元素组成（数字、大写字母、小写字母、特殊字符），且必须包含全部四种元素；密码长度大于等于8个字符。
        if (!PasswordValidatorUtils.isValid(userPassword)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "密码由四种元素组成（数字、大写字母、小写字母、特殊字符），且必须包含全部四种元素；密码长度大于等于8个字符");
        }

        //第二次有失败次数及以后才查询缓存
        Object mes = redisUtil.get("user:" + userAccount + ":info");
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        User user = new User();
        if (mes == null) {
            // 比对输入的账号和密码
            queryWrapper.eq("userAccount", userAccount);
            user = this.getOne(queryWrapper);
            // 查不到用户
            if (user == null) {
                log.info("userInfo select fail");
                throw new BusinessException(ErrorCode.PARAM_ERROR, "查无此用户");
            }
            redisUtil.set("user:" + userAccount + ":info", JsonUtil.obj2String(user), 120);
        }


        //检查用户是否被锁定
        boolean result = checkIsLock(userAccount);
        String key = "user:" + userAccount + ":failCount";
        String jsonUser = (String) redisUtil.get("user:" + userAccount + ":info");
        if (StringUtils.isNotBlank(jsonUser)) {
            user = JsonUtil.string2Obj(jsonUser, User.class);
        }
        boolean saltverifyMD5 = MD5Util.getSaltverifyMD5(userPassword, user.getUserPassword(), user.getSalt());
        // 密码不匹配
        if (!saltverifyMD5) {
            this.setFailCount(userAccount);
            Object o = redisUtil.get(key);
            int count = (int) o;

            if (count == 5) {
                //判断是否已经达到了最大失败次数,达到就重置
                String lockkey = "user:" + userAccount + ":lockTime";
                redisUtil.set(lockkey, "1", 2 * 60 * 60);//设置锁定时间为2小时


                HashMap<String, String> msg = new HashMap<>();
                msg.put("phone",user.getPhone());
                msg.put("code","123456");
                rabbitTemplate.convertAndSend("ali.sms.exchange","ali.verify.code",msg);

                redisUtil.del(key);
                redisUtil.del("user:" + userAccount + ":info");

                throw new BusinessException(ErrorCode.PARAM_ERROR, "当前账号已经被锁定，请在2小时之后尝试");
            }

            count = 5 - count;

            throw new BusinessException(ErrorCode.PARAM_ERROR, ("登陆失败，您还剩" + count + "次登录机会"));

        }
        //密码匹配
        redisUtil.del(key);
        redisUtil.del("user:" + userAccount + ":info");

        //脱敏后的用户信息
        User safeUser = getSafetyUser(user);

        // request.getSession().setAttribute(UserConstant.USER_LOGIN_STATUS,safeUser);

        return safeUser;
    }


    /**
     * 是否被锁定
     *
     * @param userAccount
     * @return
     */
    private boolean checkIsLock(String userAccount) {
        long lockTime = this.getUserLoginTimeLock(userAccount);
        String key = "user:" + userAccount + ":failCount";
        if (lockTime > 0) {//判断用户是否已经被锁定
            String desc = "该账号已经被锁定,请在" + lockTime + "秒之后尝试";
            // return map;
            throw new BusinessException(ErrorCode.PARAM_ERROR, desc);
        }
        return true;

    }

    /**
     * 设置失败次数
     *
     * @param username username
     */
    private void setFailCount(String username) {
        long count = this.getUserFailCount(username);
        String key = "user:" + username + ":failCount";
        if (count < 0) {//判断redis中是否有该用户的失败登陆次数，如果没有，设置为1，过期时间为2分钟，如果有，则次数+1
            redisUtil.set(key, 1, 120);
        } else {
            redisUtil.incr(key, 1);
        }
    }

    /**
     * 获取当前用户已失败次数
     *
     * @param username username
     * @return 已失败次数
     */
    private int getUserFailCount(String username) {
        String key = "user:" + username + ":failCount";
        //从redis中获取当前用户已失败次数
        Object object = redisUtil.get(key);
        if (object != null) {
            return (int) object;
        } else {
            return -1;
        }
    }

    /**
     * 检查用户是否已经被锁定，如果是，返回剩余锁定时间，如果否，返回-1
     *
     * @param userAccount
     * @return 时间
     */
    private int getUserLoginTimeLock(String userAccount) {
        String key = "user:" + userAccount + ":lockTime";
        int lockTime = (int) redisUtil.getExpireSeconds(key);
        if (lockTime > 0) {//查询用户是否已经被锁定，如果是，返回剩余锁定时间，如果否，返回-1
            return lockTime;
        } else {
            return -1;
        }
    }

    /**
     * 得到脱敏后的用户
     *
     * @param originUser
     * @return
     */
    public User getSafetyUser(User originUser) {
        User safeUser = new User();
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
        safeUser.setPlanetCode(originUser.getPlanetCode());
        return safeUser;
    }

    /**
     * 用户注销
     *
     * @param request
     */
    @Override
    public void userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(UserConstant.USER_LOGIN_STATUS);

    }


    /**
     * 根据用户名查询
     *
     * @param username 用户名
     * @return
     */
    public List<User> searchByUserName(String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        //用户名为空时，默认查询全部用户,否则，根据用户名查询
        return this.list(queryWrapper);

    }
}





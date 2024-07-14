package com.xiaofan.usercenter.redis;

import com.xiaofan.usercenter.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

@SpringBootTest
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisUtil redisUtil;

    @Test
    void test(){
        System.out.println(redisTemplate);
    }

    /**
     * 检查用户是否已经被锁定，如果是，返回剩余锁定时间，如果否，返回-1
     * @param id  username
     * @return  时间
     */
    private int getUserLoginTimeLock(long id) {
        String key = "user:" + id + ":lockTime";
        int lockTime = (int)redisUtil.getExpireSeconds(key);
        if(lockTime > 0){//查询用户是否已经被锁定，如果是，返回剩余锁定时间，如果否，返回-1
            return lockTime;
        }else{
            return -1;
        }
    }

    /**
     * 设置失败次数
     * @param username  username
     */
    private void setFailCount(String username){
        long count = this.getUserFailCount(username);
        String key = "user:" + username + ":failCount";
        if(count < 0){//判断redis中是否有该用户的失败登陆次数，如果没有，设置为1，过期时间为2分钟，如果有，则次数+1
            redisUtil.set(key,1,120);
        }else{
            // redisUtil.incr(key,new Double(1));
        }
    }

    /**
     * 获取当前用户已失败次数
     * @param username  username
     * @return  已失败次数
     */
    private int getUserFailCount(String username){
        String key = "user:" + username + ":failCount";
        //从redis中获取当前用户已失败次数
        Object object = redisUtil.get(key);
        if(object != null){
            return (int)object;
        }else{
            return -1;
        }
    }


}

package com.xiaofan.usercenter.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@Slf4j
@EnableCaching
public class RedisConfiguration {
    //
    // @Bean
    // public RedisTemplate redisTemplate(RedisConnectionFactory redisConnectionFactory){
    //     log.info("开始创建redis模板对象");
    //     RedisTemplate redisTemplate = new RedisTemplate();
    //     //设置redis连接工厂对象
    //     redisTemplate.setConnectionFactory(redisConnectionFactory);
    //     //设置redis key的序列号器
    //     redisTemplate.setKeySerializer(new StringRedisSerializer());
    //
    //    redisTemplate.setValueSerializer (new StringRedisSerializer());
    //
    //
    //     return redisTemplate;
    //
    // }


    @Bean(name = "redisTemplate")
    public RedisTemplate<String,Object> redisTemplate(RedisConnectionFactory factory){
        RedisTemplate<String,Object> template = new RedisTemplate<>();
        //配置连接工厂
        template.setConnectionFactory(factory);
        //使用jackson序列化和反序列value的值，
        Jackson2JsonRedisSerializer jacksonSerializer = new Jackson2JsonRedisSerializer(Object.class);
        ObjectMapper mapper = new ObjectMapper();
        //指定需要序列化的范围，All表示field、get和set，以及修饰符范围，ANY表示所有范围，包括private
        mapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        jacksonSerializer.setObjectMapper(mapper);
        //设置template中value使用Jackson2JsonRedisSerializer序列化
        template.setValueSerializer(jacksonSerializer);
        //设置template中key使用StringRedisSerializer序列化
        template.setKeySerializer(new StringRedisSerializer());
        //这是hash中key和value的序列化方式,key采用StringRedisSerializer，value采用Jackson2JsonRedisSerializer
        template.setHashKeySerializer(new StringRedisSerializer());
        template.setHashValueSerializer(jacksonSerializer);
        //使template属性生效
        template.afterPropertiesSet();
        return template;
    }

}

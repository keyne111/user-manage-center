package com.xiaofan.usercenter.config;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.xiaofan.usercenter.common.ErrorCode;
import com.xiaofan.usercenter.exception.BusinessException;
import com.xiaofan.usercenter.utils.SmsUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Map;

/**
 * 对象转换器
 */
@Configuration
@Slf4j
public class RabbitConfig {

    @Autowired
    private SmsUtils smsUtils;

    @Autowired
    private SmsProperties prop;

    @Bean
    public MessageConverter messageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(name = "delay.queue", durable = "true"),
            exchange = @Exchange(name = "delay.direct", type = ExchangeTypes.DIRECT, delayed = "true"),
            key = "delay"
    ))
    public void listenerDelayMessage(Map<String, String> msg)  {
        log.info("监听到延迟消息:{}", msg);
        String phone = msg.get("phone");
        String code = msg.get("code");
        SendSmsResponse resp = null;
        try {
            resp = this.smsUtils.sendSms(phone, code, prop.getSignName(), prop.getVerifyCodeTemplate());
            if (!resp.getCode().equals("OK")) {
                throw new BusinessException("状态:"+resp.getCode()+"消息:"+resp.getMessage(),40004);
                // throw new RuntimeException("状态:"+resp.getCode()+"消息:"+resp.getMessage());
            }
        } catch (ClientException e) {
            e.printStackTrace();
            throw new RuntimeException("短信发送出现异常问题:"+e.getMessage());
        }

    }
}
package com.xiaofan.usercenter.listener;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.xiaofan.usercenter.config.SmsProperties;
import com.xiaofan.usercenter.utils.SmsUtils;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.annotation.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SmsListener {

    @Autowired
    private SmsUtils smsUtils;

    @Autowired
    private SmsProperties prop;

    @Resource
    private RabbitTemplate rabbitTemplate;

    // @RabbitListener(bindings = @QueueBinding(
    //         value = @Queue(value = "ali.sms.queue", durable = "true"),
    //         exchange = @Exchange(value = "ali.sms.exchange", ignoreDeclarationExceptions = "true",type = ExchangeTypes.DIRECT),
    //         key={"ali.verify.code"}))
    // public void listenSms(Map<String, String> msg) throws ClientException {
    //     if(CollectionUtils.isEmpty(msg))
    //     {
    //         //放弃处理
    //         return;
    //     }
    //     String phone=msg.get("phone");
    //     String code=msg.get("code");
    //
    //     if (StringUtils.isBlank(phone)||StringUtils.isBlank(code)){
    //         return;
    //     }
    //     SendSmsResponse resp  =this.smsUtils.sendSms(phone,code,prop.getSignName(),prop.getVerifyCodeTemplate());
    // }

    @RabbitListener(queuesToDeclare = @Queue(
            name="simple.queue",
            durable = "true",
            arguments = @Argument(name = "x-queue-mode",value = "lazy")
    ))
    public void listenSms2(Map<String, String> msg)  {
        if(CollectionUtils.isEmpty(msg))
        {
            //放弃处理
            return;
        }
        String phone=msg.get("phone");
        String code=msg.get("code");

        if (StringUtils.isBlank(phone)||StringUtils.isBlank(code)){
            return;
        }

        //利用延迟队列1分钟后发送消息，监听短信服务发送，失败后重试1次，还失败，投到error队列了
        SendSmsResponse resp  = null;
        try {
            resp = this.smsUtils.sendSms(phone,code,prop.getSignName(),prop.getVerifyCodeTemplate());
            if(!resp.getCode().equals("OK")){
                rabbitTemplate.convertAndSend("delay.direct", "delay", msg, new MessagePostProcessor() {
                    @Override
                    public Message postProcessMessage(Message message) throws AmqpException {
                        //添加延迟消息属性
                        // message.getMessageProperties().setDelay(60000);
                        message.getMessageProperties().setDelay(1000);
                        return message;
                    }
                });
            }
        } catch (ClientException e) {
            e.printStackTrace();
            throw new RuntimeException("短信发送出现异常问题:"+e.getMessage());
        }


    }
}

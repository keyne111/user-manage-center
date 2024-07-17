package com.xiaofan.usercenter.listener;

import com.aliyuncs.dysmsapi.model.v20170525.SendSmsResponse;
import com.aliyuncs.exceptions.ClientException;
import com.github.xiaoymin.knife4j.core.util.CollectionUtils;
import com.xiaofan.usercenter.config.SmsProperties;
import com.xiaofan.usercenter.utils.SmsUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.amqp.core.ExchangeTypes;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
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

    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = "ali.sms.queue", durable = "true"),
            exchange = @Exchange(value = "ali.sms.exchange", ignoreDeclarationExceptions = "true",type = ExchangeTypes.DIRECT),
            key={"ali.verify.code"}))
    public void listenSms(Map<String, String> msg) throws ClientException {
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
        SendSmsResponse resp  =this.smsUtils.sendSms(phone,code,prop.getSignName(),prop.getVerifyCodeTemplate());
    }
}

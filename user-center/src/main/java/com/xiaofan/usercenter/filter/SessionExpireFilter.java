package com.xiaofan.usercenter.filter;

import cn.hutool.core.util.StrUtil;

import com.xiaofan.usercenter.common.Const;
import com.xiaofan.usercenter.model.domain.User;
import com.xiaofan.usercenter.utils.CookieUtil;
import com.xiaofan.usercenter.utils.JsonUtil;
import com.xiaofan.usercenter.utils.RedisUtil;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;

/**
 * 过滤器，重置redis中session有效期
 */
@Component
@WebFilter(urlPatterns = "/*",filterName = "sessionExporeFilter")
public class SessionExpireFilter implements Filter {
	/**
	这里有一个坑，这里我们使用了RedisUtil大家发现这里并没有用@Autowird注解注入
	是因为注入不进来，和spring的启动顺序有关，我们需要在init方法中引入，如果没有引入就是空指针异常
	*/
    private RedisUtil redisUtil;
    /**
    在这里获取ApplicationContext对象，通过name或者type获取redisUtil赋值给redis变量否则就是空指针
    */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        ApplicationContext context = WebApplicationContextUtils.getWebApplicationContext(filterConfig.getServletContext());
        redisUtil = (RedisUtil)context.getBean("redisUtil");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        //读取loginToken
        String loginToken = CookieUtil.readLoginToken(httpServletRequest);

        if (StrUtil.isNotBlank(loginToken)) {
            //从redis中获取
            String jsonStr = (String)redisUtil.get(loginToken);
            //转换
            User user = JsonUtil.string2Obj(jsonStr, User.class);
            if (user != null) {
                //重置时间
                redisUtil.expire(loginToken, Const.REDIS_SESSION_EXPIRE);
            }
        }
        chain.doFilter(request,response);
    }

    @Override
    public void destroy() {

    }
}

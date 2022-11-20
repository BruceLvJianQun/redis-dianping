package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * 用户登录拦截器，在操作业务的时候，先进行拦截，判断其是否登录
 *
 */
public class RefreshTokenInterceptor implements HandlerInterceptor {
    private StringRedisTemplate stringredisTemplate;
    public RefreshTokenInterceptor(StringRedisTemplate stringredisTemplate) {
        this.stringredisTemplate = stringredisTemplate;
    }

    //前置拦截
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //1.获取请求头中的token
        String token = request.getHeader("authorization");
       if (StrUtil.isBlank(token)){
            return true;
       }

       String key = RedisConstants.LOGIN_USER_KEY + token;
       Map<Object, Object> userMap = stringredisTemplate.opsForHash().entries(key);
       if (userMap.isEmpty()){
           return true;
        }

       //5.将查询到的Hash数据转换成UserDTO对象、然后存储到ThreadLocal
        UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
        UserHolder.saveUser(userDTO);//保存用户信息到TreadLoad中
        //6.刷新token的有效期
        stringredisTemplate.expire(key,RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);
        return true;//放行
    }
}

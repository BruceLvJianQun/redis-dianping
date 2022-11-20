package com.hmdp.utils;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.StrUtil;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.FlashMap;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.security.Key;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 *
 * 用户登录拦截器，在操作业务的时候，先进行拦截，判断其是否登录
 *
 */
public class LoginInterceptor implements HandlerInterceptor {

    //这里不可以使用注解来是实现对象的创建，是因为这个类是我们自己创建的，而且不属于spring的管里，所以需要我们自己来实现对象的创建
    //这里我们就使用构造函数来进行对象的创建
    //private StringRedisTemplate stringredisTemplate;
    //public LoginInterceptor(StringRedisTemplate stringredisTemplate) {
    //    this.stringredisTemplate = stringredisTemplate;
    //}

    //前置拦截
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
       // //1.获取session
       // //HttpSession session = request.getSession();
       // //1.获取请求头中的token
       // String token = request.getHeader("authorization");
       //if (StrUtil.isBlank(token)){
       //     response.setStatus(401);
       //     return false;
       //}
       //
       // //2.获取session中用户
       // //Object user = session.getAttribute("user");
       // //2.基于token获取redis中的用户
       //  String key = RedisConstants.LOGIN_USER_KEY + token;
       // Map<Object, Object> userMap = stringredisTemplate.opsForHash().entries(key);
       //
       // //3.判断用户是否存在
       // //if (user == null){
       // //    //4.不存在，拦截,返回401
       // //    response.setStatus(401);
       // //    return false;
       // //}
       // //3.判断用户是否存在
       // if (userMap.isEmpty()){
       //     // 4.不存在，拦截,返回401
       //     response.setStatus(401);
       //     return false;
       // }
       //
       // //5.存在的话，保存用户信息到ThreadLocal
       // //UserHolder.saveUser((UserDTO)user);
       // //5.将查询到的Hash数据转换成UserDTO对象、然后存储到ThreadLocal
       // UserDTO userDTO = BeanUtil.fillBeanWithMap(userMap, new UserDTO(), false);
       // UserHolder.saveUser(userDTO);//保存用户信息到TreadLoad中
       //
       // //6.放行
       // //return true;
       // //6.刷新token的有效期
       // stringredisTemplate.expire(key,RedisConstants.LOGIN_USER_TTL, TimeUnit.MINUTES);

        //1.判断ThreadLoad中是否具有用户，判断是否有必要拦截
        if (UserHolder.getUser() == null){
            //没有就需要拦截
            response.setStatus(401);
            return false;
        }
        //有用户就放行
        return true;//放行
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //移除用户
        UserHolder.removeUser();
    }
}

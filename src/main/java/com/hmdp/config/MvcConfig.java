package com.hmdp.config;

import com.hmdp.utils.LoginInterceptor;
import com.hmdp.utils.RefreshTokenInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.annotation.Resource;

@Configuration
public class MvcConfig implements WebMvcConfigurer {
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //registry是拦截器的注册器
        //registry.addInterceptor(new LoginInterceptor(stringRedisTemplate))
        //        //不需要拦截的请求
        //        .excludePathPatterns(
        //                "/user/code",
        //                "/user/login",
        //                "/blog/hot",
        //                "/shop/**",
        //                "/shop-type/**",
        //                "/upload/**",
        //                //优惠券相关的请求
        //                "/voucher/**"
        //        );
       registry.addInterceptor(new RefreshTokenInterceptor(stringRedisTemplate))
               .addPathPatterns("/**")
               .order(0);

        registry.addInterceptor(new LoginInterceptor())
                //不需要拦截的请求
                .excludePathPatterns(
                        "/user/code",
                        "/user/login",
                        "/blog/hot",
                        "/shop/**",
                        "/shop-type/**",
                        "/upload/**",
                        //优惠券相关的请求
                        "/voucher/**"
                )
                .order(1);//执行顺序，数字越小越先执行
    }
}

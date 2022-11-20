package com.hmdp.service.impl;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import cn.hutool.core.util.RandomUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;
import com.hmdp.mapper.UserMapper;
import com.hmdp.service.IUserService;
import com.hmdp.utils.RegexUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.hmdp.utils.RedisConstants.*;
import static com.hmdp.utils.SystemConstants.USER_NICK_NAME_PREFIX;

/**
 * <p>
 * 服务实现类
 * </p>
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public Result sendCode(String phone, HttpSession session) {
        //1.校验手机号
        if (RegexUtils.isPhoneInvalid(phone)){
            //2.如果无效手机号，返回错误信息
            return Result.fail("手机号格式错误");
        }
        //3，如果符合，生成验证码；使用hutool工具包，生成6位数的随机验证码
        String code = RandomUtil.randomNumbers(6);

        //4.保存验证码到session
        //session.setAttribute("code",code);
        //stringRedisTemplate.opsForValue().set("login:code" + phone,code,2, TimeUnit.MINUTES);
        stringRedisTemplate.opsForValue().set(LOGIN_CODE_KEY + phone,code,LOGIN_CODE_TTL, TimeUnit.MINUTES);

        //5.验证码发送给用户
        //真的要发验证码需要调用第三方的接口，这里就模拟发成功了，等后面再实现真的发送短信或者邮箱验证码
        log.info("短信验证码发送成功,验证码是:{}" + code);
        //返回ok
        return Result.ok();
    }

    @Override
    public Result login(LoginFormDTO loginForm, HttpSession session) {
        //1.校验手机号
        String phone = loginForm.getPhone();
        if (RegexUtils.isPhoneInvalid(phone)){
            //2.如果不符合，返回错误信息
            return Result.fail("手机号格式错误");
        }

        //3.校验验证码是否一致
        //Object cacheCode = session.getAttribute("code");//session中的验证码
        //String code = loginForm.getCode();//输入的验证码

        //3.升级：从redis中获取验证码并校验是否一致
        String cacheCode = stringRedisTemplate.opsForValue().get(LOGIN_CODE_KEY + phone);
        String code = loginForm.getCode();//输入的验证码

        //验证码不一致的话，就报错
        //if (cacheCode == null || !cacheCode.toString().equals(code)){
        //    log.info("验证码错误");
        //    return Result.fail("验证码错误");
        //}

        //升级：验证码不一致的话，就报错
        if (cacheCode == null || !cacheCode.equals(code)){
            log.info("验证码错误");
            return Result.fail("验证码错误");
        }

        // 4.一致的时候，根据手机号查询用户:select * from tb_user where phone = ?
        User user = query().eq("phone", phone).one();

        //5.判断用户是否存在，不存在，创建新用户，然后保存到session
        if (user == null){
            //6.说明用户之前也不存在，那么我们要创建新用户，保存新用户
            user = createUserWithPhone(phone);
        }


        //7.无论上面用户存在与否，最后都要将用户写入session
        //UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        //session.setAttribute("user", userDTO);
        //return Result.ok();

        /**
         * 7.升级：将用户信息存储到redis中，分三步进行存储，使用Hash数据结构进行存储
         *      7.1.随机生成token,作为登录令牌
         *      7.2.将user对象转换为Hash存储
         *      7.3.存储
         *      7.4.设置token的有效期时间
         */
        String token = UUID.randomUUID().toString(true);//7.1
        UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
        //报错： java.lang.Long cannot be cast to java.lang.String
       // Map<String, Object> userMap = BeanUtil.beanToMap(userDTO);7.2将User对象转换为HashMap存储
        Map<String, Object> userMap = BeanUtil.beanToMap(userDTO,new HashMap<>(),
                CopyOptions.create()
                        .setIgnoreNullValue(true)//忽略所有的nul值
                        .setFieldValueEditor((fieldName,fieldValue) -> fieldValue.toString())
                );
        String tokenKey = LOGIN_USER_KEY + token;
        stringRedisTemplate.opsForHash().putAll(tokenKey,userMap);//7.3.存储
        //7.4.设置token有效期
        // 只要用户不操作了，就开始计时开始30分钟，如果用户在不断的访问，就需要随时更新这个有效期
        stringRedisTemplate.expire(tokenKey,LOGIN_USER_TTL,TimeUnit.MINUTES);
        /**
         * 8.返回token给前端
         */
        return Result.ok(token);
    }

    /**
     *
     * @param phone
     * @return user
     * 这个方法是用来存储新用户信息的
     */
    private User createUserWithPhone(String phone) {
        //1.创建用户
        User user = new User();
        user.setPhone(phone);
        user.setNickName(USER_NICK_NAME_PREFIX + RandomUtil.randomString(10));
        //2.保存用户
        save(user);
        return user;
    }
}

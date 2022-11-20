package com.hmdp.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hmdp.dto.LoginFormDTO;
import com.hmdp.dto.Result;
import com.hmdp.entity.User;

import javax.servlet.http.HttpSession;

/**
 * <p>
 *  服务类
 * </p>
 *
 */
public interface IUserService extends IService<User> {
    //1.业务层接口中定义发送短信的抽象接口
    Result sendCode(String phone, HttpSession session);

    //2.登录
    Result login(LoginFormDTO loginForm, HttpSession session);
}

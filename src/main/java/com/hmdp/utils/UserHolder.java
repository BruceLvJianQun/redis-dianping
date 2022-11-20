package com.hmdp.utils;

import com.hmdp.dto.UserDTO;
import com.hmdp.entity.User;

/*
    ThreadLocal的使用
 */
public class UserHolder {
    //静态常量
    private static final ThreadLocal<UserDTO> tl = new ThreadLocal<>();
    //静态方法
    public static void saveUser(UserDTO user){
        tl.set(user);
    }

    public static UserDTO getUser(){
        return tl.get();
    }

    public static void removeUser(){
        tl.remove();
    }
}

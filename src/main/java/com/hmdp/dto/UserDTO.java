package com.hmdp.dto;

import lombok.Data;

/**
 *
 * 存储返回前端的一些必要信息
 *
 */
@Data
public class UserDTO {
    private Long id;
    private String nickName;
    private String icon;
}

package com.xiaofan.usercenter.model.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(allowableValues = "用户登录传递的数据模型")
public class UserLoginDto implements Serializable {
    private static final long serialVersionUID = 4940721528577475829L;

    @Schema(allowableValues = "用户账号")
    private String userAccount;
    @Schema(allowableValues = "用户密码")
    private String userPassword;

}

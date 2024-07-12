package com.xiaofan.usercenter.model.domain.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serializable;

@Data
@Schema(allowableValues = "用户注册时传递的数据模型")
public class UserRegisterDto implements Serializable {

    private static final long serialVersionUID = -5241995110244274472L;

    @Schema(allowableValues = "用户账号")
    private String userAccount;

    @Schema(allowableValues = "用户密码")
    private String userPassword;

    @Schema(allowableValues = "校验密码")
    private String checkPassword;

    @Schema(allowableValues = "星球编号")
    private String planetCode;


}

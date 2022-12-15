package com.punchin.dto;

import com.punchin.enums.Platform;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

@Data
public class LoginRequestDTO {

    @ApiModelProperty(value = "This is used to describe user id which is able to authenticate")
    private String userId;

    @ApiModelProperty(value = "This is used to describe password of user")
    private String password;

    @ApiModelProperty(value = "This is used to describe which platform used for login")
    private Platform platform;
}

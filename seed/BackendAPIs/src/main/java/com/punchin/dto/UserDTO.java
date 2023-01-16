package com.punchin.dto;

import com.punchin.enums.RoleEnum;
import com.punchin.enums.UserStatus;
import lombok.Data;

@Data
public class UserDTO {

    private Long id;

    private String userId;

    private String firstName;

    private String lastName;

    private UserStatus status;

    private boolean isAccountLocked;

    private RoleEnum role;

    private String aadharCardNumber;
}

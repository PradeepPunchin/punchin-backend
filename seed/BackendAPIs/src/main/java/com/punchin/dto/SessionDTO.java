package com.punchin.dto;

import com.punchin.enums.Platform;
import lombok.Data;

import java.util.Date;

@Data
public class SessionDTO {
    private Long id;

    private UserDTO user;

    private String authToken;

    private Date startTime;

    private Platform platform;

    private boolean isMobile = false;

    private String deviceId;

    private Long lastActiveTime;
}

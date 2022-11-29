package com.punchin.Service;

import com.punchin.dto.LoginRequestDTO;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

public interface AuthenticationService {
    Map<String, Object> authenticateUserAccount(LoginRequestDTO credentials);

    void logout(HttpServletRequest request, Map<String, Object> result);
}

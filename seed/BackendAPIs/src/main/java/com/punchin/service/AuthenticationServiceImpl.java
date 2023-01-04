package com.punchin.service;

import com.punchin.dto.LoginRequestDTO;
import com.punchin.dto.SessionDTO;
import com.punchin.entity.User;
import com.punchin.enums.Platform;
import com.punchin.enums.RoleEnum;
import com.punchin.repository.UserRepository;
import com.punchin.utility.ModelMapper;
import com.punchin.utility.constant.Headers;
import com.punchin.utility.constant.MessageCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@Transactional
public class AuthenticationServiceImpl implements AuthenticationService{

    @Autowired
    UserRepository userRepository;

    @Autowired
    SessionService sessionService;

    @Autowired
    private ModelMapper modelMapper;

    @Override
    public Map<String, Object> authenticateUserAccount(LoginRequestDTO credentials) {
        log.info("AuthenticationServiceImpl :: authenticateUserAccount credentials{}", credentials);
        Map<String, Object> mapResult = new HashMap<>();
        User user = userRepository.findByUserIdIgnoreCase(credentials.getUserId());
        if(Objects.isNull(user)){
            mapResult.put("message", MessageCode.INVALID_USERID);
            return mapResult;
        }
        if(user.getPassword() != null && BCrypt.checkpw(credentials.getPassword(), user.getPassword())){
            if(user.getRole().equals(RoleEnum.AGENT) && credentials.getPlatform().equals(Platform.WEB)){
                mapResult.put("message", MessageCode.unauthorized);
            }else {
                mapResult.put("session", modelMapper.map(sessionService.createSession(user, credentials), SessionDTO.class));
                mapResult.put("message", MessageCode.success);
            }
        }else{
            mapResult.put("message", MessageCode.INVALID_PASSWORD);
        }
        return mapResult;
    }

    @Override
    public void logout(HttpServletRequest request, Map<String, Object> result) {
        log.info("AuthenticationServiceImpl :: logout request{}, result{}", request, result);
        String sessionId = request.getHeader(Headers.AUTH_TOKEN);
        if(Objects.isNull(sessionId)){
            log.info("AuthenticationServiceImpl :: logout SESSION IS NULL");
            Cookie[] cookies = request.getCookies();
            if (cookies != null && cookies.length > 0) {
                sessionId = Arrays.stream(cookies).filter(cookie -> cookie.getName().equals(Headers.AUTH_TOKEN)).findFirst().map(Cookie::getValue).orElse(sessionId);
            }
        }
        if(Objects.nonNull(sessionId)){
            sessionService.deleteByAuthToken(sessionId);
        }
    }
}

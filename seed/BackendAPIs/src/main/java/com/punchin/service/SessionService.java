package com.punchin.service;

import com.punchin.dto.LoginRequestDTO;
import com.punchin.entity.Session;
import com.punchin.entity.User;
import com.punchin.enums.Platform;

public interface SessionService {

    Session findByAuthToken(String authenticationToken);

    void expireSessionAtAnyTime(String authenticationToken);

    Session createSession(User user, LoginRequestDTO credentials);

    void deleteByAuthToken(String authToken);

    void updateSessionTimeOut(Session session);
}

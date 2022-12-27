package com.punchin.service;

import com.punchin.entity.Session;
import com.punchin.entity.User;
import com.punchin.enums.Platform;

public interface SessionService {

    Session findByAuthToken(String authenticationToken);

    void expireSessionAtAnyTime(String authenticationToken);

    Session createSession(User user, Platform platform);

    void deleteByAuthToken(String authToken);

    void updateSessionTimeOut(Session session);
}

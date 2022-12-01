package com.punchin.service;

import com.punchin.entity.Session;
import com.punchin.entity.User;

public interface SessionService {

    Session findByAuthToken(String authenticationToken);

    void expireSessionAtAnyTime(String authenticationToken);

    Session createSession(User user);

    void deleteByAuthToken(String authToken);
}

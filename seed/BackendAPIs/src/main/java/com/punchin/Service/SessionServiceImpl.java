package com.punchin.Service;

import com.punchin.entity.Session;
import com.punchin.entity.User;
import com.punchin.enums.Platform;
import com.punchin.repository.SessionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Service
@Transactional
public class SessionServiceImpl implements SessionService{

    @Autowired
    private SessionRepository sessionRepository;
    @Override
    public Session findByAuthToken(String authenticationToken) {
        return sessionRepository.findByAuthToken(authenticationToken);
    }

    @Override
    public void expireSessionAtAnyTime(String authenticationToken) {
        sessionRepository.deleteByAuthToken(authenticationToken);
    }

    @Override
    public Session createSession(User user) {
        Session session = new Session();
        final long currentTimeMillis = System.currentTimeMillis();
        session.setStartTime(new Date(currentTimeMillis));
        session.setAuthToken(UUID.randomUUID().toString());

        //Change when it is required.
        session.setMobile(false);
        session.setPlatform(Platform.WEB);
        session.setDeviceId("test");

        session.setLastActiveTime(currentTimeMillis);
        session.setUser(user);
        return sessionRepository.save(session);
    }

    @Override
    public void deleteByAuthToken(String authToken) {
        sessionRepository.deleteByAuthToken(authToken);
    }
}

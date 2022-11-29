package com.punchin.bootstrap;

import com.punchin.entity.User;
import com.punchin.enums.RoleEnum;
import com.punchin.enums.UserStatus;
import com.punchin.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class Bootstrap implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Handle an application event.
     *
     * @param event the event to respond to
     */
    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        log.info("In On Application Event Service");
        try {
            createDefaultUserIfNotExist();
        } catch (Exception e) {
            log.error("Exception In On Application Event Service - ", e);
        }
    }

    private void createDefaultUserIfNotExist() {
        log.info("In Create Admin If Not Exist Service");
        try {
            List<User> users = new ArrayList<>();
            if (!userRepository.existsByUserIdIgnoreCase("admin")) {
                User user = new User();
                user.setUserId("admin");
                user.setFirstName("admin");
                user.setLastName("test");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("123"));
                user.setRole(RoleEnum.ROLE_ADMIN);
                users.add(user);
            }
            if (!userRepository.existsByUserIdIgnoreCase("banker")) {
                User user = new User();
                user.setUserId("banker");
                user.setFirstName("banker");
                user.setLastName("test");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("123"));
                user.setRole(RoleEnum.ROLE_BANKER);
                users.add(user);
            }
            if (!userRepository.existsByUserIdIgnoreCase("verifier")) {
                User user = new User();
                user.setUserId("verifier");
                user.setFirstName("verifier");
                user.setLastName("test");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("123"));
                user.setRole(RoleEnum.ROLE_VERIFIER);
                users.add(user);
            }
            if (!userRepository.existsByUserIdIgnoreCase("agent")) {
                User user = new User();
                user.setUserId("agent");
                user.setFirstName("agent");
                user.setLastName("test");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("123"));
                user.setRole(RoleEnum.ROLE_AGENT);
                users.add(user);
            }
            userRepository.saveAll(users);
        } catch (Exception e) {
            log.error("Exception In Create Admin If Not Exist Service - ", e);
        }
    }

}

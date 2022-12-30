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
                user.setPassword(passwordEncoder.encode("admin@123"));
                user.setRole(RoleEnum.ADMIN);
                users.add(user);
            }
            if (!userRepository.existsByUserIdIgnoreCase("banker")) {
                User user = new User();
                user.setUserId("banker");
                user.setFirstName("A Bank");
                user.setLastName("test");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("banker@123"));
                user.setRole(RoleEnum.BANKER);
                users.add(user);

                user = new User();
                user.setUserId("banker2");
                user.setFirstName("B Bank");
                user.setLastName("test");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("banker@123"));
                user.setRole(RoleEnum.BANKER);
                users.add(user);

                user = new User();
                user.setUserId("banker3");
                user.setFirstName("C Bank");
                user.setLastName("test");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("banker@123"));
                user.setRole(RoleEnum.BANKER);
                users.add(user);
            }
            if (!userRepository.existsByUserIdIgnoreCase("verifier")) {
                User user = new User();
                user.setUserId("verifier");
                user.setFirstName("A Verifier");
                user.setLastName("test");
                user.setState("UP");
                user.setCity("UP");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("verifier@123"));
                user.setRole(RoleEnum.VERIFIER);
                user.setState("UP");
                users.add(user);

                user = new User();
                user.setUserId("verifier2");
                user.setFirstName("B Verifier");
                user.setLastName("test");
                user.setState("DL");
                user.setCity("DL");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("verifier@123"));
                user.setRole(RoleEnum.VERIFIER);
                user.setState("DL");
                users.add(user);

                user = new User();
                user.setUserId("verifier3");
                user.setFirstName("C Verifier");
                user.setLastName("test");
                user.setState("PB");
                user.setCity("PB");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("verifier@123"));
                user.setRole(RoleEnum.VERIFIER);
                user.setState("PB");
                users.add(user);
            }
            if (!userRepository.existsByUserIdIgnoreCase("agent")) {
                User user = new User();
                user.setUserId("agent");
                user.setFirstName("agent");
                user.setLastName("test");
                user.setState("UP");
                user.setCity("UP");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("agent@123"));
                user.setRole(RoleEnum.AGENT);
                user.setState("UP");
                users.add(user);

                user = new User();
                user.setUserId("agent2");
                user.setFirstName("agent2");
                user.setLastName("test");
                user.setState("DL");
                user.setCity("DL");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("agent@123"));
                user.setRole(RoleEnum.AGENT);
                user.setState("DL");
                users.add(user);

                user = new User();
                user.setUserId("agent3");
                user.setFirstName("agent3");
                user.setLastName("test");
                user.setState("PB");
                user.setCity("PB");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("agent@123"));
                user.setRole(RoleEnum.AGENT);
                user.setState("PB");
                users.add(user);
            }
            userRepository.saveAll(users);
        } catch (Exception e) {
            log.error("Exception In Create Admin If Not Exist Service - ", e);
        }
    }

}

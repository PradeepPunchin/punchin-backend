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
            createDefaultAgentIfNotExist();
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
            if (!userRepository.existsByUserIdIgnoreCase("super.banker")) {
                User user = new User();
                user.setUserId("super.banker");
                user.setFirstName("Super Banker");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("super@123"));
                user.setRole(RoleEnum.SUPER_BANKER);
                users.add(user);
            }
            if (!userRepository.existsByUserIdIgnoreCase("banker")) {
                User user = new User();
                user.setUserId("lkart");
                user.setFirstName("Lendingkart");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("lkart@123"));
                user.setRole(RoleEnum.BANKER);
                users.add(user);
            }
            if (!userRepository.existsByUserIdIgnoreCase("verifier")) {
                User user = new User();
                user.setUserId("chandramouli.banerjee");
                user.setFirstName("OURS Group");
                user.setCity("West Bengal");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("banerjee@123"));
                user.setRole(RoleEnum.VERIFIER);
                user.setState("West Bengal");
                users.add(user);

                user = new User();
                user.setUserId("sangeetha");
                user.setFirstName("AVI Business Solutions");
                user.setCity("Karnataka");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("sangeetha@123"));
                user.setRole(RoleEnum.VERIFIER);
                user.setState("Karnataka");
                users.add(user);

                user = new User();
                user.setUserId("pranav.bansal");
                user.setFirstName("Infominer");
                user.setState("PB");
                user.setCity("PB");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("bansal@123"));
                user.setRole(RoleEnum.VERIFIER);
                user.setState("Delhi");
                users.add(user);
            }
            userRepository.saveAll(users);
        } catch (Exception e) {
            log.error("Exception In Create Admin If Not Exist Service - ", e);
        }
    }

    private void createDefaultAgentIfNotExist() {
        log.info("In Create Admin If Not Exist Service");
        try {
            List<User> users = new ArrayList<>();
            if (!userRepository.existsByUserIdIgnoreCase("agent")) {
                User user = new User();
                user.setUserId("manisha.silelan");
                user.setFirstName("Manisha");
                user.setLastName("Silelan");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("manisha@123"));
                user.setRole(RoleEnum.AGENT);
                user.setState("West Bengal");
                user.setCity("West Bengal");
                user.setVerifierId(userRepository.findTopByStateIgnoreCaseOrderById(user.getState().toLowerCase()));
                users.add(user);

                user = new User();
                user.setUserId("pradeep.kumar");
                user.setFirstName("Pradeep");
                user.setLastName("Kumar");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("pradeep@123"));
                user.setRole(RoleEnum.AGENT);
                user.setState("Karnataka");
                user.setCity("Karnataka");
                user.setVerifierId(userRepository.findTopByStateIgnoreCaseOrderById(user.getState().toLowerCase()));
                users.add(user);

                user = new User();
                user.setUserId("tarun.jangra");
                user.setFirstName("Tarun");
                user.setLastName("Jangra");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("tarun@123"));
                user.setRole(RoleEnum.AGENT);
                user.setState("DELHI");
                user.setCity("New Delhi");
                user.setVerifierId(userRepository.findTopByStateIgnoreCaseOrderById(user.getState().toLowerCase()));
                users.add(user);

                /*user = new User();
                user.setUserId("agent3");
                user.setFirstName("Gurpreet");
                user.setLastName("Singh");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("agent@123"));
                user.setRole(RoleEnum.AGENT);
                user.setState("PUNJAB");
                user.setCity("Firozpur");
                user.setVerifierId(userRepository.findTopByStateIgnoreCaseOrderById(user.getState().toLowerCase()));
                users.add(user);*/
            }
            userRepository.saveAll(users);
        } catch (Exception e) {
            log.error("Exception In Create Admin If Not Exist Service - ", e);
        }
    }

}

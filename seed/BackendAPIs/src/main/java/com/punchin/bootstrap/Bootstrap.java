package com.punchin.bootstrap;

import com.punchin.entity.Roles;
import com.punchin.entity.User;
import com.punchin.enums.DefaultRoles;
import com.punchin.enums.UserStatus;
import com.punchin.repository.RoleRepository;
import com.punchin.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.*;

@Slf4j
@Component
public class Bootstrap implements ApplicationListener<ContextRefreshedEvent> {

    @Autowired
    private RoleConfig roleConfig;

    @Autowired
    private RoleRepository roleRepository;

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
            createRolesIfNotExist();
            createDefaultUserIfNotExist();
        } catch (Exception e) {
            log.error("Exception In On Application Event Service - ", e);
        }
    }

    private void createDefaultUserIfNotExist() {
        log.info("In Create Admin If Not Exist Service");
        try {
            List<User> users = new ArrayList<>();
            if (!userRepository.existsByEmail("super_admin@yopmail.com")) {
                User user = new User();
                user.setEmail("super_admin@yopmail.com");
                user.setFirstName("super_admin");
                user.setLastName("test");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("123"));
                user.setRole(roleRepository.findByName(String.valueOf(DefaultRoles.SUPER_ADMIN)));
                user.setUserName("Super-Admin");
                users.add(user);
            }
            if (!userRepository.existsByEmail("admin@yopmail.com")) {
                User user = new User();
                user.setEmail("admin@yopmail.com");
                user.setFirstName("admin");
                user.setLastName("test");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("123"));
                user.setRole(roleRepository.findByName(String.valueOf(DefaultRoles.ROLE_ADMIN)));
                user.setUserName("Admin");
                users.add(user);
            }
            if (!userRepository.existsByEmail("banker@yopmail.com")) {
                User user = new User();
                user.setEmail("banker@yopmail.com");
                user.setFirstName("banker");
                user.setLastName("test");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("123"));
                user.setRole(roleRepository.findByName(String.valueOf(DefaultRoles.ROLE_BANKER)));
                user.setUserName("Banker");
                users.add(user);
            }
            if (!userRepository.existsByEmail("verifier@yopmail.com")) {
                User user = new User();
                user.setEmail("verifier@yopmail.com");
                user.setFirstName("verifier");
                user.setLastName("test");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("123"));
                user.setRole(roleRepository.findByName(String.valueOf(DefaultRoles.ROLE_VERIFIER)));
                user.setUserName("Verifier");
                users.add(user);
            }
            if (!userRepository.existsByEmail("agent@yopmail.com")) {
                User user = new User();
                user.setEmail("agent@yopmail.com");
                user.setFirstName("agent");
                user.setLastName("test");
                user.setAccountLocked(false);
                user.setStatus(UserStatus.ACTIVE);
                user.setPassword(passwordEncoder.encode("123"));
                user.setRole(roleRepository.findByName(String.valueOf(DefaultRoles.ROLE_AGENT)));
                user.setUserName("Agent");
                users.add(user);
            }
            userRepository.saveAll(users);
        } catch (Exception e) {
            log.error("Exception In Create Admin If Not Exist Service - ", e);
        }
    }

    private void createRolesIfNotExist() {
        log.info("In Create Roles If Not Exist Service");
        try {
            JSONArray configuredRoles = roleConfig.getRoles();
            List<Roles> roleList = new ArrayList<>();
            Pageable limit = PageRequest.of(0, 1);
            Page<Roles> alreadySavedRoles = roleRepository.findAll(limit);
            if (alreadySavedRoles.getTotalElements() == 0) {
                configuredRoles.forEach(item -> {
                    JSONObject obj = (JSONObject) item;
                    Roles role = new Roles();
                    role.setName(obj.get("name").toString());
                    role.setDescription(obj.get("description").toString());
                    role.setRepresentationName(obj.get("representationName").toString());
                    roleList.add(role);

                });
                roleRepository.saveAll(roleList);
            }
        } catch (Exception e) {
            log.error("Exception In Create Roles If Not Exist Service - ", e);
        }
    }
}

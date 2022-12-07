package com.punchin.service;

import com.punchin.entity.User;
import com.punchin.enums.RoleEnum;
import com.punchin.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Slf4j
@Service
@Transactional
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepository;

    @Override
    public User findAgentById(Long agentId) {
        try{
            log.info("UserServiceImpl :: findAgentById agentId {}", agentId);
            return userRepository.findByIdAndRole(agentId, RoleEnum.AGENT);
        }catch (Exception e){
            log.error("EXCEPTION WHILE UserServiceImpl :: findAgentById e {}", e);
            return null;
        }
    }
}

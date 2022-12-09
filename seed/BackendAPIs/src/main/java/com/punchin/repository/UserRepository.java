package com.punchin.repository;

import com.punchin.entity.User;
import com.punchin.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUserIdIgnoreCase(String userId);
    User findByUserIdIgnoreCase(String userId);
    User findByIdAndRole(Long agentId, RoleEnum role);
    boolean existsByIdAndRole(Long id, String banker);
}

package com.punchin.repository;

import com.punchin.entity.User;
import com.punchin.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUserIdIgnoreCase(String userId);
    User findByUserIdIgnoreCase(String userId);
    User findByIdAndRole(Long agentId, RoleEnum role);
    boolean existsByIdAndRole(Long id, RoleEnum banker);

    @Query(nativeQuery = true, value = "SELECT u.id FROM users AS u LEFT JOIN claims_data AS cd ON cd.agent_id = u.id WHERE u.role =:roles and LOWER(u.state) =:borrowerState GROUP BY u.id ORDER BY count(cd.agent_id)")
    User findByAgentAndState(RoleEnum roles, String borrowerState);
}

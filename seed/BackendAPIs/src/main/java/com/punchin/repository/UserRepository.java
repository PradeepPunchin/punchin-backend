package com.punchin.repository;

import com.punchin.entity.User;
import com.punchin.enums.RoleEnum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUserIdIgnoreCase(String userId);

    User findByUserIdIgnoreCase(String userId);

    User findByIdAndRole(Long agentId, RoleEnum role);

    boolean existsByIdAndRole(Long id, RoleEnum banker);

    @Query(nativeQuery = true, value = "SELECT u.* FROM users AS u LEFT JOIN claims_data AS cd ON cd.agent_id = u.id WHERE u.role =:roles and LOWER(u.state) =:borrowerState GROUP BY u.id ORDER BY count(cd.agent_id)")
    User findByAgentAndState(String roles, String borrowerState);

    @Query(nativeQuery = true, value = "select * from users u where u.id =:verifierId and u.role = 'VERIFIER'")
    User verifierExistsByIdAndRole(@Param("verifierId") long verifierId);

    @Query(nativeQuery = true, value = "select * from users u where u.role = 'AGENT' and u.status = 'ACTIVE' and u.state =:state ")
    List<User> findAllAgentsForVerifier(@Param("state") String state);

    @Query(nativeQuery = true, value = "select exists(select * from users u where u.role = 'AGENT' and u.status = 'ACTIVE' and u.id =:agentId and u.state =:state) ")
    Boolean findAgentState(@Param("agentId") Long agentId, @Param("state") String state);

    @Query(nativeQuery = true, value = "SELECT id FROM users WHERE role = 'VERIFIER' and LOWER(state)=LOWER((SELECT state FROM pin_code_state WHERE pin_code=:pinCode LIMIT 1))")
    Long findByPinCode(String pinCode);

    Boolean existsByIdAndVerifierId(Long agentId, Long id);

    @Query(nativeQuery = true, value = "SELECT id FROM users WHERE LOWER(state)=:state and role = 'VERIFIER' LIMIT 1")
    Long findTopByStateIgnoreCaseOrderById(String state);

    @Query(nativeQuery = true, value = "SELECT EXISTS(SELECT id FROM users WHERE role = 'VERIFIER' and LOWER(state)=LOWER((SELECT state FROM pin_code_state WHERE pin_code=:pinCode)) LIMIT 1)")
    boolean existsByPinCode(String pinCode);

    @Query(nativeQuery = true, value = "SELECT id FROM users WHERE role = 'BANKER'")
    List<Long> getAllBankerIds();
}

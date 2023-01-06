package com.punchin.repository;

import com.punchin.entity.AgentVerifierRemark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AgentVerifierRemarkRepository extends JpaRepository<AgentVerifierRemark, Long> {
    List<AgentVerifierRemark> findByClaimIdOrderById(Long id);
}

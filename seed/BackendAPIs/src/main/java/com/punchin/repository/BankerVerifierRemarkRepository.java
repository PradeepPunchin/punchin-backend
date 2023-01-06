package com.punchin.repository;

import com.punchin.entity.BankerVerifierRemark;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BankerVerifierRemarkRepository extends JpaRepository<BankerVerifierRemark, Long> {

    List<BankerVerifierRemark> findByClaimIdOrderById(Long id);
}

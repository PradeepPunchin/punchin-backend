package com.punchin.repository;

import com.punchin.entity.ClaimDraftData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ClaimDraftDataRepository extends JpaRepository<ClaimDraftData, Long> {

    void deleteByPunchinBankerId(String userId);

    List<ClaimDraftData> findAllByPunchinBankerId(String userId);

    Page findAllByPunchinBankerId(String userId, Pageable pageable);
}

package com.punchin.repository;

import com.punchin.entity.ClaimDraftData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimUploadDraftRepository extends JpaRepository<ClaimDraftData, Long> {

}

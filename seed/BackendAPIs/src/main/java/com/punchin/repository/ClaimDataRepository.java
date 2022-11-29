package com.punchin.repository;

import com.punchin.entity.ClaimData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface ClaimDataRepository extends JpaRepository<ClaimData, Integer> {

    @Query(nativeQuery = true , value =" select * from claim_data")
    Page<ClaimData> findAllClaimData(Pageable pageable);

}

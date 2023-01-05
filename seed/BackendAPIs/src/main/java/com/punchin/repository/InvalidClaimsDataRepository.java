package com.punchin.repository;

import com.punchin.entity.InvalidClaimsData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InvalidClaimsDataRepository extends JpaRepository<InvalidClaimsData, Long> {
}

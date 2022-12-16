package com.punchin.repository;

import com.punchin.entity.ClaimAllocated;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;

@Repository
public interface ClaimAllocatedRepository extends JpaRepository<ClaimAllocated, Long> {

}

package com.punchin.repository;

import com.punchin.entity.PinCodeState;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PinCodeStateRepository extends JpaRepository<PinCodeState, Long> {
    boolean existsByPinCode(String trim);
}

package com.punchin.repository;

import com.punchin.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session, Long> {

    Session findByAuthToken(String authenticationToken);

    @Modifying
    //@Query(nativeQuery = true, value = "DELETE FROM session WHERE authToken = :authenticationToken")
    void deleteByAuthToken(String authenticationToken);
}

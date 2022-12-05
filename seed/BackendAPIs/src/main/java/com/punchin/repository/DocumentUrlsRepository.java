package com.punchin.repository;

import com.punchin.entity.DocumentUrls;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentUrlsRepository extends JpaRepository<DocumentUrls, Long> {
}

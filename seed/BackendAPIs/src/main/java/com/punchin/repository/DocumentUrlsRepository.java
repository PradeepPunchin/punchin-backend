package com.punchin.repository;

import com.punchin.entity.DocumentUrls;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DocumentUrlsRepository extends JpaRepository<DocumentUrls, Long> {
    List<DocumentUrls> findDocumentUrls(Long id);
}

package com.punchin.repository;

import com.punchin.entity.Roles;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RoleRepository extends JpaRepository<Roles, Long> {

    Page<Roles> findAll(Pageable page);

    Roles findByName(String roleName);
}

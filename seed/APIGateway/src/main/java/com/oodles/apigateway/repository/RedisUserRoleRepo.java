package com.oodles.apigateway.repository;

import com.oodles.apigateway.domain.RedisUserRole;
import org.springframework.data.repository.CrudRepository;

public interface RedisUserRoleRepo extends CrudRepository<RedisUserRole, String> {
    RedisUserRole findByToken(String token);

}

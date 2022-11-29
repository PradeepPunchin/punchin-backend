package com.oodles.apigateway.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

import java.io.Serializable;
import java.util.List;

@Data
@RedisHash(timeToLive = 3600)
public class RedisUserRole implements Serializable {
    private static final long serialVersionUID = -2601175617857390837L;
    /**
	 * @return the userId
	 */
	public String getUserId() {
		return userId;
	}

	/**
	 * @param userId the userId to set
	 */
	public void setUserId(String userId) {
		this.userId = userId;
	}

	/**
	 * @return the token
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * @return the roles
	 */
	public List<String> getRoles() {
		return roles;
	}

	/**
	 * @param roles the roles to set
	 */
	public void setRoles(List<String> roles) {
		this.roles = roles;
	}

	@Id
    private String userId;
    @Indexed
    private String token;
    private List<String> roles;

    /* public static RedisUserRole of(UserDto user){
         RedisUserRole userRole = new RedisUserRole();
         userRole.userId = user.getId();
         userRole.token = user.getToken();
         userRole.roles = Arrays.asList("ROLE_USER", "ROLE_USER_VIEW");
         return userRole;
     }
 */
    public static RedisUserRole of(String userId, String token, List<String> roles){
        RedisUserRole userRole = new RedisUserRole();
        userRole.userId = userId;
        userRole.token = token;
        userRole.roles = roles;
        return userRole;
    }

    @Override
    public String toString() {
        return "{ userId='" + userId + '\'' +
                ", token='" + token + '\'' +
                ", roles=" + roles +
                '}';
    }
}

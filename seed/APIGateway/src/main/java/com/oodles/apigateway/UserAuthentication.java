package com.oodles.apigateway;

import com.oodles.apigateway.domain.RedisUserRole;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.stream.Collectors;

public class UserAuthentication implements Authentication {
    private static final long serialVersionUID = -8524087869422730778L;

    private RedisUserRole user;

    public UserAuthentication(){}
    public UserAuthentication(RedisUserRole user){
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
          return user.getRoles().stream().map(role -> new SimpleGrantedAuthority(role))
                .collect(Collectors.toList());
    }

    @Override
    public Object getCredentials() {
        return user.getToken();
    }

    @Override
    public Object getDetails() {
        return user;
    }

    @Override
    public Object getPrincipal() {
        return user;
    }

    @Override
    public boolean isAuthenticated() {
        return true;
    }

    @Override
    public void setAuthenticated(boolean b) throws IllegalArgumentException {

    }

    @Override
    public boolean equals(Object o) {
        return false;
    }

    @Override
    public String toString() {
        return user.toString();
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public String getName() {
        return user.getUserId();
    }
}

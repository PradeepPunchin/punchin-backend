package com.oodles.apigateway;

import com.oodles.apigateway.domain.RedisUserRole;
import com.oodles.apigateway.repository.RedisUserRoleRepo;
import lombok.extern.slf4j.Slf4j;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;
import java.util.Objects;


@Slf4j
public class AuthenticationFilter extends GenericFilterBean {
	
	private static final Logger log=LoggerFactory.getLogger(AuthenticationFilter.class);
   // private MongoTemplate mongoTemplate;
    // private RedisUserRoleTemplate redisUserRoleTemplate;
    private RedisUserRoleRepo redisRepo;

   // private JwtTokenProvider jwtTokenProvider;

    public AuthenticationFilter( RedisUserRoleRepo redisRepo){
       // this.mongoTemplate = mongoTemplate;
        this.redisRepo = redisRepo;
       // this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        String token = request.getHeader("Authorization");
        log.info("In Authentication filter, Token {}", token);

        if(!StringUtils.isEmpty(token)){
           RedisUserRole userRole = redisRepo.findByToken(token);
            //RedisUserRole userRole = RedisUserRole.of("1", token, Arrays.asList("ROLE_USER"));
            log.info("User role {}", userRole);
            if(!Objects.isNull(userRole)){
                UserAuthentication authentication = new UserAuthentication(userRole);
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        }
        chain.doFilter(request, response);
    }


}

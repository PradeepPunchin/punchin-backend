package com.oodles.apigateway;

import java.io.IOException;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;


@Component
public class JwtTokenAuthenticationFilter extends OncePerRequestFilter {

	private static final Logger LOGGER=LoggerFactory.getLogger(JwtTokenAuthenticationFilter.class);
	
	@Autowired
	private JwtConfig jwtConfig;

	@Autowired
	private RedisTemplate<String, String> redisTemplate;

	public JwtTokenAuthenticationFilter() {
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {
		LOGGER.info("header = "+jwtConfig.getHeader());
		LOGGER.info("Expiration = "+jwtConfig.getExpiration());
		LOGGER.info("Secret = "+jwtConfig.getSecret());
		String header = request.getHeader(jwtConfig.getHeader());

		if (header == null) {
			chain.doFilter(request, response); // If not valid, goto the next filter.
			return;
		}
		String token = header;
		try {
			Claims claims = Jwts.parser().setSigningKey(jwtConfig.getSecret().getBytes()).parseClaimsJws(token)
					.getBody();
			String username = claims.getSubject();
			String jtoken = redisTemplate.opsForValue().get(username);
			if (username != null && jtoken != null && jtoken.equals(header)) {
				Date now = new Date();
				Date validity = new Date(now.getTime() + jwtConfig.getExpiration());
				redisTemplate.expireAt(username, validity);
				@SuppressWarnings("unchecked")
				List<String> authorities = (List<String>) claims.get("auth");
		//		authorities.stream().map(authority -> new SimpleGrantedAuthority(authority)).filter(Objects::nonNull).collect(Collectors.toList());
				UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(username, null,authorities.stream().map(authority -> new SimpleGrantedAuthority(authority)).filter(Objects::nonNull).collect(Collectors.toList()));
				SecurityContextHolder.getContext().setAuthentication(auth);
			}

		} catch (Exception e) {
			SecurityContextHolder.clearContext();
		}

		chain.doFilter(request, response);
	}

}

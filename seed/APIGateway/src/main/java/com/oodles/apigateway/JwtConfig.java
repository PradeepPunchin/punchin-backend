package com.oodles.apigateway;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class JwtConfig {
    @Value("${jwt.uri}")
    private String Uri;

    @Value("${jwt.header.name}")
    private String header;

    @Value("${jwt.token.expire.milliseconds}")
    private int expiration;

    @Value("${jwt.secret.key}")
    private String secret;
    
    public JwtConfig() {
    	
    }

	public String getUri() {
		return Uri;
	}

	public void setUri(String uri) {
		Uri = uri;
	}

	public String getHeader() {
		return header;
	}

	
	public int getExpiration() {
		return expiration;
	}

	

	public String getSecret() {
		return secret;
	}

	
	
}
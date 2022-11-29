package com.punchin.security;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Customized the security Response
 *
 * @author Rahul Sharma
 */
@Component
public class RESTAuthenticationEntryPoint implements AuthenticationEntryPoint {

    public static final String UNAUTHORISED = "Unauthorised";

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
        response.sendError(HttpServletResponse.SC_UNAUTHORIZED, UNAUTHORISED);

    }

}

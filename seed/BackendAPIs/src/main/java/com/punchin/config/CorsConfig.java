package com.punchin.config;

import com.punchin.utility.constant.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * CORS Configuration. This filter is used to add the CORS configuration to the current request.
 *
 */

@Configuration
@Order(Ordered.HIGHEST_PRECEDENCE)
public class CorsConfig extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(CorsConfig.class);
    private static final String ALLOWED_METHODS = "GET, POST, PUT, DELETE, OPTIONS";
    private static final String ALLOWED_HEADERS = "Content-Type,X-preferedLanguage,RequestTimeZone,X-XSRF-TOKEN,RequestTime,X-Xsrf-Token,AcceptLanguage";
    private static final String ALLOWED_CREDENTIALS = "true";
    private static final String ALLOWED_ORIGIN = "*";

    @Override
    protected void doFilterInternal(final HttpServletRequest request, final HttpServletResponse response,
                                    final FilterChain filterChain) throws ServletException, IOException {
        response.addHeader(Headers.ACCESS_CONTROL_ALLOW_ORIGIN, ALLOWED_ORIGIN);
        if (HttpMethod.OPTIONS.toString().equalsIgnoreCase(request.getMethod())) {
            response.addHeader(Headers.ACCESS_CONTROL_ALLOW_METHODS, ALLOWED_METHODS);
            response.addHeader(Headers.ACCESS_CONTROL_ALLOW_HEADERS, ALLOWED_HEADERS);
            response.addHeader(Headers.ACCESS_CONTROL_ALLOW_CREDENTIALS, ALLOWED_CREDENTIALS);
            response.setStatus(HttpServletResponse.SC_OK);
        } else {
            filterChain.doFilter(request, response);
        }
    }


}


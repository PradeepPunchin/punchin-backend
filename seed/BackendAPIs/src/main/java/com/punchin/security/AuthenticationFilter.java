package com.punchin.security;

import com.punchin.entity.CustomUserDetails;
import com.punchin.service.SessionService;
import com.punchin.entity.Session;
import com.punchin.utility.constant.Headers;
import com.punchin.utility.constant.Literals;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

/**
 * Authentication Filter to authorize the request based on X-Xsrf-Token
 *
 */
@Slf4j
public class AuthenticationFilter extends GenericFilterBean {

    public static final String REQUEST_ID = "requestId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String authenticationToken = httpServletRequest.getHeader(Headers.AUTH_TOKEN);
        if (!httpServletRequest.getMethod().equals("OPTIONS") && authenticationToken != null && !authenticationToken.isEmpty() && !"null".equals(authenticationToken)) {
            SessionService sessionService = WebApplicationContextUtils.getRequiredWebApplicationContext(httpServletRequest.getServletContext()).getBean(SessionService.class);
            Session session = sessionService.findByAuthToken(authenticationToken);
            if (session != null) {
                if (checkSessionTimeOut(session)) {
                    sessionService.expireSessionAtAnyTime(authenticationToken);
                }else {
                    sessionService.updateSessionTimeOut(session);
                    userNamePasswordAuthentication(authenticationToken, session);
                }
            }
            chain.doFilter(request, response);
        }
    }

    private void userNamePasswordAuthentication(String authenticationToken, Session session) {
        final UsernamePasswordAuthenticationToken usernamePasswordAuthenticationToken = new UsernamePasswordAuthenticationToken(session.getUser(),
                null, new CustomUserDetails(session.getUser()).getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        AbstractAuthenticationToken auth = (AbstractAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        HashMap<String, Object> info = new HashMap<>();
        info.put(Headers.AUTH_TOKEN, authenticationToken);
        MDC.put(REQUEST_ID, UUID.randomUUID().toString());
        auth.setDetails(info);
    }

    private boolean checkSessionTimeOut(Session session) {
        try {
            if (session.getLastActiveTime() != null && (System.currentTimeMillis() - session.getLastActiveTime() >= Literals.HALF_HOUR)) {
                return true;
            }
        } catch (Exception e) {
            log.error("error in authentication filter check session time out - {}", e.getMessage());
        }
        return false;
    }
}

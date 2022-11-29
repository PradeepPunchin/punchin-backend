package com.punchin.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDecisionManager;
import org.springframework.security.access.vote.AffirmativeBased;
import org.springframework.security.access.vote.RoleVoter;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.method.configuration.GlobalMethodSecurityConfiguration;

/**
 * Configure the role according to spring security.
 *
 * @author Rahul Sharma
 */
@Configuration
@EnableGlobalMethodSecurity(securedEnabled = true)
public class RoleAuthorityConfig extends GlobalMethodSecurityConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(RoleAuthorityConfig.class);

    @Override
    protected AccessDecisionManager accessDecisionManager() {
        AffirmativeBased accessDecisionManager = (AffirmativeBased) super.accessDecisionManager();
        //Remove the ROLE_ prefix from RoleVoter for @Secured and hasRole checks on methods
        accessDecisionManager.getDecisionVoters().parallelStream()
                .filter(RoleVoter.class::isInstance)
                .map(RoleVoter.class::cast)
                .forEachOrdered(it -> it.setRolePrefix(""));
        logger.info(".......Role Authority Set Successfully.........");
        return accessDecisionManager;
    }
}

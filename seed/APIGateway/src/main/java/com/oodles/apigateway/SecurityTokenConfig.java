package com.oodles.apigateway;

import com.oodles.apigateway.repository.RedisUserRoleRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityTokenConfig extends WebSecurityConfigurerAdapter {

	/*@Autowired
	private JwtConfig jwtConfig;

	@Autowired
	JwtTokenAuthenticationFilter jwtTokenAuthenticationFilter;

	@Override
	protected void configure(HttpSecurity http) throws Exception {

		http.headers().defaultsDisabled().disable();
		http.csrf().disable()
				.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
				.exceptionHandling()
				.authenticationEntryPoint((req, rsp, e) -> rsp.sendError(HttpServletResponse.SC_UNAUTHORIZED)).and()
				.addFilterAfter(jwtTokenAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.authorizeRequests()
				.antMatchers(HttpMethod.POST, jwtConfig.getUri()).permitAll()
				.anyRequest().hasAuthority("PERMISSION_LIST_USER");

	}*/


    //    @Autowired
//    RedisUserRoleTemplate redisUserRoleTemplate;
//
    @Autowired
    RedisUserRoleRepo redisUserRoleRepo;

	/*@Autowired
	JwtTokenProvider jwtTokenProvider;*/

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        http.cors().disable().csrf().disable().sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                .and()
                .authorizeRequests().anyRequest()
                .authenticated();

        http.addFilterBefore(new AuthenticationFilter(redisUserRoleRepo), BasicAuthenticationFilter.class);
    }

    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring().antMatchers("/v2/api-docs", "/configuration/ui", "/swagger-resources/**",
                "/configuration/security", "/swagger-ui.html", "/webjars/**", "/static/**");

        web.ignoring().antMatchers("/hystrix/images/*");
        web.ignoring().antMatchers("/hystrix");

        web.ignoring().antMatchers("/api/v1/user/register", "/api/v1/user/login");
        web.ignoring().antMatchers("/api/v1/admin/register", "/api/v1/admin/login");

        web.ignoring().antMatchers("/*/")
                .antMatchers("/eureka/**")
                .antMatchers(HttpMethod.OPTIONS, "/**");
    }


}

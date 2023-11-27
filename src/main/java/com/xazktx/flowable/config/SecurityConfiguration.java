package com.xazktx.flowable.config;

import org.flowable.ui.common.security.SecurityConstants;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * 绕过 Flowable 授权
 */
@Configuration
public class SecurityConfiguration {

    @Configuration(proxyBeanMethods = false)
    @Order(SecurityConstants.FORM_LOGIN_SECURITY_ORDER - 1)
    public static class FormLoginWebSecurityConfigurerAdapter extends WebSecurityConfigurerAdapter {

        @Override
        protected void configure(HttpSecurity http) throws Exception {
            http.csrf()
                    .disable()
                    .authorizeRequests()
                    .antMatchers("/modeler/**", "/admin/**", "/idm/**", "/task/**", "/admin-app/**",
                            "/app/**", "/idm-app/**", "/*-api/**", "/api/editor/**")
                    .permitAll()
                    .and()
                    .headers()
                    .frameOptions()
                    .disable();
        }

    }

}

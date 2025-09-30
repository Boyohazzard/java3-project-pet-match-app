package edu.java3projectpetmatchapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractAuthenticationFilterConfigurer;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception{
        return httpSecurity.authorizeHttpRequests(registry->{
                // Public pages and static resources (needed for Bootstrap)
                    registry.requestMatchers("/index", "/", "/home", "/register", "/login").permitAll();
                    registry.requestMatchers("/css/**", "/js/**", "/images/**").permitAll();

                // Roll-based access
                    registry.requestMatchers("/admin/**").hasRole("ADMIN");
                    registry.requestMatchers("/staff/**").hasRole("STAFF");
                    registry.requestMatchers("/user/**").hasRole("USER");

                    registry.anyRequest().authenticated();
                })

                .formLogin(form -> form.loginPage("/login").permitAll())
                .build();
    }

    @Bean
    public UserDetailsService userDetailService() {
        UserDetails normalLoser = User.builder()
                .username("jg")
                .password("$2a$12$fawUS1QwPlasB/EX3cHFg.uScJtmWP8qGTOWGLxpKGRsW7leu/K4e")
                .roles("USER", "STAFF")
                .build();
        UserDetails adminUser = User.builder()
                .username("admin")
                .password("$2a$12$3w1e5ZpXEQ5hptmR5LJEuulA9yvbHG7i1JMRd/8BSC.a5UsPqeVku")
                .roles("ADMIN", "STAFF", "USER")
                .build();
        return new InMemoryUserDetailsManager(normalLoser, adminUser);
    }

    @Bean
    PasswordEncoder passwordEncoder () {
        return new BCryptPasswordEncoder();
    }
}

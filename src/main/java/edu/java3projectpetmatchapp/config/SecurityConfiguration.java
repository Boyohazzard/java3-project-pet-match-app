package edu.java3projectpetmatchapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(registry -> {
                    registry.requestMatchers("/index", "/", "/home", "/register", "/login", "/error").permitAll();
                    registry.requestMatchers("/css/**", "/js/**", "/images/**").permitAll();
                    registry.requestMatchers(HttpMethod.GET, "/register").permitAll();
                    registry.requestMatchers(HttpMethod.POST, "/register").permitAll();
                    registry.requestMatchers("/admin/**").hasRole("ADMIN");
                    registry.requestMatchers("/staff/**").hasRole("STAFF");
                    registry.anyRequest().authenticated();

                })
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .successHandler(customSuccessHandler())
                        .failureUrl("/login?error")
                        //.defaultSuccessUrl("/index", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .permitAll()
                )
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    PasswordEncoder passwordEncoder () {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public RoleHierarchy roleHierarchy() {
        RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
        hierarchy.setHierarchy("ROLE_ADMIN > ROLE_STAFF > ROLE_USER > ROLE_GUEST");
        return hierarchy;
    }

    @Bean
    public AuthenticationSuccessHandler customSuccessHandler() {
        return new CustomAuthenticationSuccessHandler();
    }
        /*@Bean
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
*/
}

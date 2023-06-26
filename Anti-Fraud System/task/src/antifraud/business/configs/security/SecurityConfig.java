package antifraud.business.configs.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    @Autowired
    private final UserDetailsService userDetailsService;

    @Autowired
    private final RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests()
                .requestMatchers("/h2-console/**").permitAll()
                .requestMatchers("/actuator/shutdown").permitAll() // needs to run test

                .requestMatchers(HttpMethod.POST, "/api/auth/user").permitAll()
                .requestMatchers(HttpMethod.DELETE, "/api/auth/user/*").hasAuthority("ADMINISTRATOR")
                .requestMatchers(HttpMethod.PUT, "/api/auth/access").hasAuthority("ADMINISTRATOR")
                .requestMatchers(HttpMethod.PUT, "/api/auth/role").hasAuthority("ADMINISTRATOR")
                .requestMatchers(HttpMethod.GET, "/api/auth/list").hasAnyAuthority("ADMINISTRATOR"
                        , "SUPPORT")

                .requestMatchers(HttpMethod.POST, "/api/antifraud/transaction").hasAuthority("MERCHANT")
                .requestMatchers(HttpMethod.PUT, "/api/antifraud/transaction").hasAuthority("SUPPORT")


                .requestMatchers(HttpMethod.POST, "/api/antifraud/suspicious-ip").hasAuthority("SUPPORT")
                .requestMatchers(HttpMethod.DELETE, "/api/antifraud/suspicious-ip/*").hasAuthority("SUPPORT")
                .requestMatchers(HttpMethod.GET, "/api/antifraud/suspicious-ip").hasAuthority("SUPPORT")

                .requestMatchers(HttpMethod.POST, "/api/antifraud/stolencard").hasAuthority("SUPPORT")
                .requestMatchers(HttpMethod.DELETE, "/api/antifraud/stolencard/*").hasAuthority("SUPPORT")
                .requestMatchers(HttpMethod.GET, "/api/antifraud/stolencard").hasAuthority("SUPPORT")

                .requestMatchers(HttpMethod.GET, "/api/antifraud/history").hasAuthority("SUPPORT")
                .requestMatchers(HttpMethod.GET, "/api/antifraud/history/*").hasAuthority("SUPPORT")

                .anyRequest().permitAll()
                .and()
                .httpBasic()
                .authenticationEntryPoint(restAuthenticationEntryPoint) // Handles auth error
                .and()
                .sessionManagement()
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // no session
                .and().csrf().disable().headers().frameOptions().disable(); // for Postman, the H2 console

        http.authenticationProvider(authenticationProvider()); // Set authentication provider

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authenticationProvider = new DaoAuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setPasswordEncoder(getEncoder());
        return authenticationProvider;
    }

    @Bean
    public PasswordEncoder getEncoder() {
        return new BCryptPasswordEncoder();
    }
}
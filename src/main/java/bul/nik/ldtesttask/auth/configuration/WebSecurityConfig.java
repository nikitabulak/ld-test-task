package bul.nik.ldtesttask.auth.configuration;

import bul.nik.ldtesttask.auth.configuration.jwt.AuthTokenFilter;
import bul.nik.ldtesttask.auth.configuration.jwt.JwtExceptionFilter;
import bul.nik.ldtesttask.auth.configuration.jwt.JwtService;
import bul.nik.ldtesttask.auth.configuration.jwt.UnauthorizedEntryPointJwt;
import bul.nik.ldtesttask.user.service.UserServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UnauthorizedEntryPointJwt unauthorizedHandler;
    private final LogoutHandler logoutHandler;
    private final ApplicationContext context;

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http, PasswordEncoder passwordEncoder, UserDetailsService userDetailsService)
            throws Exception {
        return http.getSharedObject(AuthenticationManagerBuilder.class)
                .userDetailsService(userDetailsService)
                .passwordEncoder(passwordEncoder)
                .and()
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    //enables CORS
    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**").allowedMethods("PUT", "DELETE",
                        "GET", "POST", "PATCH");
            }
        };
    }

    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    protected SecurityFilterChain defaultSecurity(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .requiresChannel(channel ->
                        channel.anyRequest().requiresSecure())
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .requestMatchers()
                .antMatchers("/auth/**");
        http.logout()
                .logoutUrl("/auth/logout")
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext());
        return http.build();
    }

    @Bean
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors().and().csrf().disable()
                .requiresChannel(channel ->
                        channel.anyRequest().requiresSecure())
                .exceptionHandling().authenticationEntryPoint(unauthorizedHandler).and()
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS).and()
                .authorizeHttpRequests()
                .antMatchers("/user/**")
                .hasRole("USER")
                .antMatchers("/operator/**")
                .hasRole("OPERATOR")
                .antMatchers("/admin/**")
                .hasRole("ADMINISTRATOR")
                .antMatchers("/dadata/**")
                .hasAnyRole("USER", "ADMINISTRATOR");
        http.addFilterBefore(new AuthTokenFilter(context.getBean(JwtService.class), context.getBean(UserServiceImpl.class)),
                UsernamePasswordAuthenticationFilter.class);
        http.addFilterBefore(new JwtExceptionFilter(), AuthTokenFilter.class);
        http.logout()
                .logoutUrl("/auth/logout")
                .addLogoutHandler(logoutHandler)
                .logoutSuccessHandler((request, response, authentication) -> SecurityContextHolder.clearContext());
        return http.build();
    }
}

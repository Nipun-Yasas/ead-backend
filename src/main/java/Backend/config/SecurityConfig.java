package Backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig{

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain SecurityFilterChain(HttpSecurity httpSecurity) throws Exception{
        return httpSecurity
            .csrf(csrf -> csrf.disable())
            .formLogin(httpForm->{
                httpForm
                    .loginPage("/login").permitAll();
            })

            .authorizeHttpRequests(registry->{
                registry.requestMatchers("/api/auth/**").permitAll();
                registry.requestMatchers("/req/signup").permitAll();
                registry.requestMatchers("/login").permitAll();

                registry.requestMatchers("/swagger-ui/**").permitAll();
                registry.requestMatchers("/v3/api-docs/**").permitAll();
                registry.requestMatchers("/swagger-ui.html").permitAll();

                registry.anyRequest().authenticated();
            })

            .build();
    }
}

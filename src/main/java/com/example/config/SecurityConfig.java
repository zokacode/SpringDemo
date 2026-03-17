package com.example.config;

import com.example.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    // 注入自定義的 JWT 過濾器
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // 禁用 CSRF (因為 JWT 本身就防禦了 CSRF，且後端是無狀態的)
            .csrf(AbstractHttpConfigurer::disable)
            // 配置路徑權限
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()        // 註冊、登入路徑完全公開
                .requestMatchers("/api/admin/**").hasRole("ADMIN")  // 僅限管理員角色存取
                .anyRequest().authenticated()                       // 其他所有請求都必須經過驗證
            )
            // 設定 Session 管理策略為「無狀態」
            // 告訴 Spring Security 不要建立 Session，也不要用 Session 儲存 User 資訊
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS) // JWT 是無狀態的，不需要 Session
            )
            // 設定過濾器順序
            // 將 jwtAuthenticationFilter 放在 UsernamePasswordAuthenticationFilter 之前
            // 這樣在檢查帳密之前，會先嘗試解析 JWT
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);  
            
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 認證管理器：負責驗證用戶的帳密是否正確
     * 這是登入 API (AuthController) 必須用到的物件
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
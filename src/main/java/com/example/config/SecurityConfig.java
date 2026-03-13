package com.example.config;

import com.example.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.cache.SpringCacheBasedUserCache;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.concurrent.TimeUnit;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {

    // 注入自定義的 JWT 過濾器
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

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
    public DaoAuthenticationProvider authenticationProvider(PasswordEncoder passwordEncoder, UserCache userCache) {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder);
        authProvider.setUserCache(userCache);
        return authProvider;
    }

    @Bean
    public CacheManager cacheManager() {
        ConcurrentMapCacheManager cacheManager = new ConcurrentMapCacheManager() {
            @Override
            protected ConcurrentMapCache createConcurrentMapCache(String name) {
                // 為用戶快取設定 30 分鐘過期時間
                if ("userCache".equals(name)) {
                    return new ConcurrentMapCache(name, 1000, false) {
                        private final java.util.concurrent.ConcurrentMap<Object, Object> store = new java.util.concurrent.ConcurrentHashMap<>();
                        
                        @Override
                        public Object get(Object key) {
                            Object value = store.get(key);
                            if (value instanceof CacheEntry) {
                                CacheEntry entry = (CacheEntry) value;
                                if (System.currentTimeMillis() > entry.expiryTime) {
                                    store.remove(key);
                                    return null;
                                }
                                return entry.value;
                            }
                            return value;
                        }
                        
                        @Override
                        public void put(Object key, Object value) {
                            if (value != null) {
                                long expiryTime = System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(30);
                                store.put(key, new CacheEntry(value, expiryTime));
                            }
                        }
                        
                        @Override
                        public void evict(Object key) {
                            store.remove(key);
                        }
                        
                        @Override
                        public void clear() {
                            store.clear();
                        }
                        
                        private static class CacheEntry {
                            final Object value;
                            final long expiryTime;
                            
                            CacheEntry(Object value, long expiryTime) {
                                this.value = value;
                                this.expiryTime = expiryTime;
                            }
                        }
                    };
                }
                return super.createConcurrentMapCache(name);
            }
        };
        
        cacheManager.setAllowNullValues(false);
        return cacheManager;
    }

    @Bean
    public UserCache userCache(CacheManager cacheManager) {
        SpringCacheBasedUserCache userCache = new SpringCacheBasedUserCache(cacheManager.getCache("userCache"));
        log.info("User cache initialized with cache manager: {}", cacheManager.getClass().getSimpleName());
        return userCache;
    }

    /**
     * 定義密碼加密工具
     * 使用 BCrypt 強雜湊演算法
     */
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
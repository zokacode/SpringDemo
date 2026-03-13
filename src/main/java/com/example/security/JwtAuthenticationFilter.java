package com.example.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserCache;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserCache userCache;

    @Override
    protected void doFilterInternal(
        HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        // 從請求頭中獲取 Authorization 欄位
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        // 從 JWT 中提取用戶名
        final String username;

        // 如果沒有 Authorization 欄位或不是 Bearer 開頭，直接放行
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 擷取 Token 字串（去掉 "Bearer " 這 7 個字元）
            jwt = authHeader.substring(7);
            
            // 驗證 token 不為空且不只有空白字元
            if (jwt == null || jwt.trim().isEmpty()) {
                logger.warn("JWT token is empty after removing Bearer prefix from request: {}", request.getRequestURI());
                filterChain.doFilter(request, response);
                return;
            }
            
            // 從 Token 中解析出用戶帳號
            username = jwtUtil.getUsernameFromToken(jwt);
        } catch (StringIndexOutOfBoundsException e) {
            logger.error("Invalid Authorization header format: {} from request: {}", e.getMessage(), request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            filterChain.doFilter(request, response);
            return;
        } catch (Exception e) {
            // 如果解析過程中發生任何異常（例如：Token 格式錯誤、過期等），直接放行
            logger.error("JWT token parsing failed: {} from request: {}", e.getMessage(), request.getRequestURI());
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            filterChain.doFilter(request, response);
            return;
        }

        // 如果有帳號，且目前的 SecurityContext 中還沒有認證資訊（避免重複驗證）
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            // 先檢查緩存中是否有用戶資料
            UserDetails userDetails = userCache.getUserFromCache(username);
            
            // 如果緩存中沒有，則從資料庫加載並放入緩存
            if (userDetails == null) {
                try {
                    userDetails = userDetailsService.loadUserByUsername(username);
                    
                    // 驗證加載的用戶資料
                    if (userDetails == null || userDetails.getUsername() == null) {
                        logger.warn("Loaded user details are invalid for username: {}", username);
                        filterChain.doFilter(request, response);
                        return;
                    }
                    
                    userCache.putUserInCache(userDetails);
                } catch (UsernameNotFoundException e) {
                    logger.warn("User not found: {} from request: {}", username, request.getRequestURI());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    filterChain.doFilter(request, response);
                    return;
                } catch (Exception e) {
                    logger.error("Error loading user details for username: {} from request: {}", username, request.getRequestURI(), e);
                    response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    filterChain.doFilter(request, response);
                    return;
                }
            }

            try {
                // 驗證 Token 是否合法
                if (jwtUtil.validateToken(jwt, userDetails)) {
                    // 建立一個認證對象（包含用戶資訊、憑證及權限清單）
                    UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                    // 設置詳細資訊（例如：IP、Session ID 等）
                    authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                    );
                    // 將驗證成功的認證對象存入 SecurityContextHolder
                    // 這樣在後續的 Controller 層就能透過 @AuthenticationPrincipal 取得當前用戶
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                    logger.debug("Successfully authenticated user: {} from request: {}", username, request.getRequestURI());
                } else {
                    logger.warn("JWT token validation failed for user: {} from request: {}", username, request.getRequestURI());
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                }
            } catch (Exception e) {
                // 如果驗證過程中發生任何異常，設置適當的狀態碼
                logger.error("JWT token validation failed: {} from request: {}", e.getMessage(), request.getRequestURI());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            }
        }
        // 繼續執行過濾鏈中的下一個步驟
        filterChain.doFilter(request, response);
    }
}

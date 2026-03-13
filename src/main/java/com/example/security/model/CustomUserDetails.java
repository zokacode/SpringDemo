package com.example.security.model;

import com.example.model.UsersModel;
import com.example.model.RolesModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.Optional;

public class CustomUserDetails implements UserDetails {

    private static final Logger logger = LoggerFactory.getLogger(CustomUserDetails.class);
    private final UsersModel users;

    public CustomUserDetails(UsersModel users) {
        this.users = users;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        // 使用 Optional 避免 NPE
        Set<RolesModel> userRoles = Optional.ofNullable(users.getRoles()).orElse(new HashSet<>());
        
        if (userRoles.isEmpty()) {
            return Set.of();
        }
        
        return userRoles.stream()
            .flatMap(role -> {
                Set<String> permissions = new HashSet<>();
                String roleName = role.getName();
                
                // 確保角色名稱有 ROLE_ 前綴
                String formattedRoleName = roleName.startsWith("ROLE_") ? roleName : "ROLE_" + roleName;
                
                switch (formattedRoleName) {
                    case "ROLE_ADMIN" -> permissions.addAll(Set.of("READ", "WRITE", "DELETE"));
                    case "ROLE_MANAGER" -> permissions.addAll(Set.of("READ", "WRITE"));
                    case "ROLE_USER" -> permissions.add("READ");
                    default -> {
                        // 對於未知角色，給予基本讀取權限並記錄警告
                        permissions.add("READ");
                        logger.warn("Unknown role detected: {}, granting basic READ permission", formattedRoleName);
                    }
                }
                return permissions.stream();
            })
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toSet());
    }

    @Override
    public String getUsername() {
        return users != null ? users.getUsername() : null;
    }

    @Override
    public String getPassword() {
        return users != null ? users.getPassword() : null;
    }

    // 是否過期
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 是否鎖定
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    // 是否憑證過期
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 是否啟用
    @Override
    public boolean isEnabled() {
        return true;
    }
}

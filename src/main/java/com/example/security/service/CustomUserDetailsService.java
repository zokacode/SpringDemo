package com.example.security.service;

import com.example.model.UsersModel;
import com.example.repository.UsersRepository;
import com.example.security.model.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {

        UsersModel user = usersRepository.findByUsername(username)
                .orElseThrow(() ->
                    new UsernameNotFoundException("User " + username + " not found"));

        return new CustomUserDetails(user);
    }
}

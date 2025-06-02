package com.microblog.service.impl;

import com.microblog.entity.User;
import com.microblog.repository.UserRepository;
import com.microblog.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    
    private final UserRepository userRepository;
    
    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrId) throws UsernameNotFoundException {
        User user;
        
        // Try to parse as ID first
        try {
            Long id = Long.parseLong(usernameOrId);
            user = userRepository.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
        } catch (NumberFormatException e) {
            // If not a number, treat as username or email
            user = userRepository.findByUsernameOrEmail(usernameOrId, usernameOrId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found with username or email: " + usernameOrId));
        }
        
        return UserPrincipal.create(user);
    }
}

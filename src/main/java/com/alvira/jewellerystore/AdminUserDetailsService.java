package com.alvira.jewellerystore;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AdminUserDetailsService implements UserDetailsService {

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AdminUser adminUser = adminUserRepository.findByUsername(username);
        if (adminUser == null) {
            throw new UsernameNotFoundException("Admin user not found: " + username);
        }
        return User.withUsername(adminUser.getUsername())
                .password(adminUser.getPassword())
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))
                .build();
    }
}
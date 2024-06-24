package com.example.cdas.services;

import com.example.cdas.config.CDASUserDetails;
import com.example.cdas.models.CDASUser;
import com.example.cdas.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CDASUserDetailsService implements UserDetailsService {
    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        CDASUser user = userRepository.findUserByEmail(email);

        if (user == null) {
            throw new UsernameNotFoundException("Could not find user");
        }
        if (!user.isEnabled()) {
            throw new UsernameNotFoundException("User is disabled");
        }
        return new CDASUserDetails(user);
    }
}

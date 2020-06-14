package com.app.service;

import com.app.entity.Author;
import com.app.repository.AuthRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private AuthRepository authRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<Author> author = authRepository.findByUsername(username);
        author.orElseThrow( () ->  new UsernameNotFoundException("Not Found: " + username));
        return author.map(MyUserDetails::new).get();
    }
}

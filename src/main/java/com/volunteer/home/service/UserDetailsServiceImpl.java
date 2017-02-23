package com.volunteer.home.service;

import com.volunteer.home.entity.User;
import com.volunteer.home.repository.MyUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Алексей on 22.02.2017.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private MyUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserDetails userDetails;
        try {
            long id;
            id = Long.parseLong(username);
            User user = userRepository.findOne(id);
            userDetails = new org.springframework.security.core.userdetails.User(
                    username, user.getPassword(), DummyAuthority.getAuth()
            );
        } catch (Exception e) {
            throw new UsernameNotFoundException(e.getMessage(), e);
        }

        return userDetails;
    }

    static class DummyAuthority implements GrantedAuthority {
        static Collection<GrantedAuthority> getAuth() {
            List<GrantedAuthority> res = new ArrayList<>();
            res.add(new DummyAuthority());
            return res;
        }

        @Override
        public String getAuthority() {
            return "ROLE_USER";
        }
    }
}

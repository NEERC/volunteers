package ru.ifmo.neerc.volunteers.service;

import ru.ifmo.neerc.volunteers.entity.User;
import ru.ifmo.neerc.volunteers.repository.MyUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Created by Алексей on 22.02.2017.
 */
@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    @Autowired
    private MyUserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        UserDetails userDetails;
        try {
            final User user = userRepository.findByEmailIgnoreCase(username);
            userDetails = new org.springframework.security.core.userdetails.User(
                    username, user.getPassword(), user.getAuth()
            );
        } catch (final Exception e) {
            throw new UsernameNotFoundException(e.getMessage(), e);
        }

        return userDetails;
    }
}

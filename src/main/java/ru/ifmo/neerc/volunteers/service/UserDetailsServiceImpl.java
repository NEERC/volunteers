package ru.ifmo.neerc.volunteers.service;

import lombok.AllArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.ifmo.neerc.volunteers.repository.UserRepository;

/**
 * Created by Алексей on 22.02.2017.
 */
@Service
@AllArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(final String username) throws UsernameNotFoundException {
        UserDetails userDetails;
        try {
            userDetails = userRepository.findByEmailIgnoreCase(username);
            /*new org.springframework.security.core.userdetails.User(
                    username, user.getPassword(), user.getAuth()
            );*/
        } catch (final Exception e) {
            throw new UsernameNotFoundException(e.getMessage(), e);
        }

        return userDetails;
    }
}

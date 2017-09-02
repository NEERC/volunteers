package ru.ifmo.neerc.volunteers.service.user;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ru.ifmo.neerc.volunteers.entity.User;
import ru.ifmo.neerc.volunteers.entity.Year;
import ru.ifmo.neerc.volunteers.repository.UserRepository;

/**
 * Created by Lapenok Akesej on 03.09.2017.
 */
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    final UserRepository userRepository;

    @Override
    public User getUserByAuthentication(final Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName());
    }

    @Override
    public void setUserYear(User user, Year year) {
        if (user.getYear() == null || user.getYear().getId() != year.getId()) {
            user.setYear(year);
            userRepository.save(user);
        }
    }
}

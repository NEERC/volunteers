package ru.ifmo.neerc.volunteers.service.user;

import org.springframework.security.core.Authentication;
import ru.ifmo.neerc.volunteers.entity.User;
import ru.ifmo.neerc.volunteers.entity.Year;

/**
 * Created by Lapenok Akesej on 03.09.2017.
 */
public interface UserService {

    User getUserByAuthentication(Authentication authentication);

    void setUserYear(User user, Year year);
}

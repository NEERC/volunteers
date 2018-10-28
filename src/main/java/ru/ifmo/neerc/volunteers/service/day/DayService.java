package ru.ifmo.neerc.volunteers.service.day;

import org.springframework.security.core.Authentication;

import ru.ifmo.neerc.volunteers.entity.Day;
import ru.ifmo.neerc.volunteers.entity.Hall;
import ru.ifmo.neerc.volunteers.entity.User;
import ru.ifmo.neerc.volunteers.entity.UserDay;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lapenok Akesej on 25.09.2017.
 */
public interface DayService {
    HashMap<Hall, List<UserDay>> getHallUser(final Day currentDay, Locale locale);

    boolean isManagerForDay(final User user, final Day day);
    boolean isManagerForDay(final Authentication authentication, final long dayId);
    boolean isManagerForUserDay(final Authentication authentication, final long userDayId);
}

package ru.ifmo.neerc.volunteers.service.day;

import ru.ifmo.neerc.volunteers.entity.Day;
import ru.ifmo.neerc.volunteers.entity.Hall;
import ru.ifmo.neerc.volunteers.entity.UserDay;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Created by Lapenok Akesej on 25.09.2017.
 */
public interface DayService {
    HashMap<Hall, List<UserDay>> getHallUser(final Day currentDay, Locale locale);
}

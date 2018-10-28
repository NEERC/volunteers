package ru.ifmo.neerc.volunteers.service.day;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import ru.ifmo.neerc.volunteers.entity.*;
import ru.ifmo.neerc.volunteers.repository.DayRepository;
import ru.ifmo.neerc.volunteers.repository.UserEventRepository;
import ru.ifmo.neerc.volunteers.service.user.UserService;
import ru.ifmo.neerc.volunteers.service.year.YearService;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Lapenok Akesej on 25.09.2017.
 */
@Service("dayService")
@AllArgsConstructor
public class DayServiceImpl implements DayService {

    final UserService userService;
    final YearService yearService;

    final UserEventRepository userEventRepository;
    final DayRepository dayRepository;

    @Override
    public HashMap<Hall, List<UserDay>> getHallUser(Day currentDay, Locale locale) {
        final Year year = currentDay.getYear();
        final Set<ApplicationForm> yearUsers = new HashSet<>(year.getUsers());
        yearUsers.removeAll(currentDay.getUsers().stream().map(UserDay::getUserYear).collect(Collectors.toSet()));
        final Hall reserve = yearService.findOrCreateDefaultHall(year, locale);

        final PositionValue defaultPosition = yearService.findOrCreateDefaultPosition(year, locale);

        userEventRepository.save(yearUsers.stream().map(af -> {
            final UserDay ue = new UserDay();
            ue.setUserYear(af);
            ue.setHall(reserve);
            ue.setPosition(defaultPosition);
            currentDay.addUser(ue);
            return ue;
        }).collect(Collectors.toList()));

        final Day day = dayRepository.save(currentDay);

        final HashMap<Hall, List<UserDay>> hallUser = new HashMap<>(
                day.getUsers().stream().collect(Collectors.groupingBy(UserDay::getHall)));
        hallUser.forEach((u, v) ->
                v.sort(Comparator
                        .comparing((Function<UserDay, Long>) d -> d.getPosition().getOrd())
                        .thenComparing(lst -> lst.getUserYear().getUser().getLastNameCyr())
                )
        );

        hallUser.putAll(year.getHalls().stream()
                .filter(h -> !hallUser.containsKey(h))
                .collect(Collectors.toMap(Function.identity(), hall -> new ArrayList<>())));

        return hallUser;
    }

    @Override
    public boolean isManagerForDay(final User user, final Day day) {
        if (user == null || day == null)
            return false;

        final Optional<UserDay> userDay = day.getUsers()
            .stream()
            .filter(u -> u.getUserYear().getUser().equals(user))
            .findFirst();

        return userDay.map(userDay1 -> userDay1.getPosition().isManager()).orElse(false);

    }

    @Override
    public boolean isManagerForDay(final Authentication authentication, final long dayId) {
        final User user = userService.getUserByAuthentication(authentication);
        final Day day = dayRepository.findOne(dayId);
        return isManagerForDay(user, day);
    }

    @Override
    public boolean isManagerForUserDay(final Authentication authentication, final long userDayId) {
        final User user = userService.getUserByAuthentication(authentication);
        final UserDay userDay = userEventRepository.findOne(userDayId);
        return isManagerForDay(user, userDay.getDay());
    }
}

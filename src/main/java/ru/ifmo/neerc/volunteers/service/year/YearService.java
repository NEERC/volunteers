package ru.ifmo.neerc.volunteers.service.year;

import ru.ifmo.neerc.volunteers.entity.*;
import ru.ifmo.neerc.volunteers.form.UserYearForm;

import java.util.Locale;
import java.util.Optional;

/**
 * Created by Lapenok Akesej on 03.09.2017.
 */
public interface YearService {
    Optional<Year> getLastYear();

    void regUser(User user, UserYearForm form, Year year);

    ApplicationForm getApplicationForm(User user, Year year);

    Year getYear(User user);

    Hall findOrCreateDefaultHall(final Year year, Locale locale);

    PositionValue findOrCreateDefaultPosition(final Year year, Locale locale);
}

package ru.ifmo.neerc.volunteers.service.year;

import ru.ifmo.neerc.volunteers.entity.User;
import ru.ifmo.neerc.volunteers.entity.Year;
import ru.ifmo.neerc.volunteers.form.UserYearForm;

import java.util.Optional;

/**
 * Created by Lapenok Akesej on 03.09.2017.
 */
public interface YearService {
    Optional<Year> getLastYear();

    void regUser(User user, UserYearForm form, Year year);
}

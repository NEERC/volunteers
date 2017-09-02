package ru.ifmo.neerc.volunteers.service.year;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.ifmo.neerc.volunteers.entity.ApplicationForm;
import ru.ifmo.neerc.volunteers.entity.User;
import ru.ifmo.neerc.volunteers.entity.Year;
import ru.ifmo.neerc.volunteers.form.UserYearForm;
import ru.ifmo.neerc.volunteers.repository.ApplicationFormRepository;
import ru.ifmo.neerc.volunteers.repository.UserRepository;
import ru.ifmo.neerc.volunteers.repository.YearRepository;

import java.util.Comparator;
import java.util.Optional;

/**
 * Created by Lapenok Akesej on 03.09.2017.
 */
@Service
@AllArgsConstructor
public class YearServiceImpl implements YearService {

    final YearRepository yearRepository;
    final ApplicationFormRepository applicationFormRepository;
    final UserRepository userRepository;

    @Override
    public Optional<Year> getLastYear() {
        return yearRepository.findAll().stream().max(Comparator.comparingLong(Year::getId));
    }

    @Override
    public void regUser(final User user, final UserYearForm form, final Year year) {
        if (user.changeUserInformation(form)) {
            userRepository.save(user);
        }
        ApplicationForm applicationForm = new ApplicationForm(form, user, year);
        applicationFormRepository.save(applicationForm);
    }
}

package ru.ifmo.neerc.volunteers.service.year;

import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;
import ru.ifmo.neerc.volunteers.entity.*;
import ru.ifmo.neerc.volunteers.form.UserYearForm;
import ru.ifmo.neerc.volunteers.repository.*;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Lapenok Akesej on 03.09.2017.
 */
@Service
@AllArgsConstructor
public class YearServiceImpl implements YearService {

    final YearRepository yearRepository;
    final ApplicationFormRepository applicationFormRepository;
    final UserRepository userRepository;
    final PositionValueRepository positionValueRepository;
    final HallRepository hallRepository;

    final MessageSource messageSource;

    @Override
    public Optional<Year> getLastYear() {
        return yearRepository.findAll().stream().max(Comparator.comparingLong(Year::getId));
    }

    @Override
    public ApplicationForm getApplicationForm(User user, Year year) {
        Optional<ApplicationForm> result = user.getApplicationForms().stream().filter(u -> u.getYear().getId() == year.getId()).findFirst();
        if (!result.isPresent()) {
            result = Optional.of(new ApplicationForm(user, year));
        }
        return result.get();
    }

    @Override
    public void regUser(final User user, final UserYearForm form, final Year year) {
        if (!user.isConfirmed())
            return;
        if (user.changeUserInformation(form)) {
            userRepository.save(user);
        }
        ApplicationForm applicationForm = getApplicationForm(user, year);
        applicationForm.setValues(form);

        applicationFormRepository.save(applicationForm);
    }

    @Override
    public Year getYear(User user) {
        if (user == null)
            return null;
        return user.getYear();
    }

    @Override
    public Hall findOrCreateDefaultHall(Year year, Locale locale) {
        Hall hall = null;
        for (final Hall hall1 : year.getHalls()) {
            if (hall1.isDef())
                hall = hall1;
        }
        if (hall == null) {
            hall = new Hall(messageSource.getMessage("volunteers.reserve.hall", null, "Reserve", locale), true, "", year);
            hallRepository.save(hall);
        }
        return hall;
    }

    @Override
    public PositionValue findOrCreateDefaultPosition(Year year, Locale locale) {
        PositionValue positionValue = null;
        for (final PositionValue positionValue1 : year.getPositionValues()) {
            if (positionValue1.isDef()) {
                positionValue = positionValue1;
            }
        }
        if (positionValue == null) {
            positionValue = new PositionValue(messageSource.getMessage("volunteers.reserve.position", null, "Reserve", locale), true, 0, year, 0L);
            positionValueRepository.save(positionValue);
        }
        return positionValue;
    }

    @PostConstruct
    public void modifyApplicationsForms() {
        Set<ApplicationForm> changeRegistrationDate = applicationFormRepository.findAll().stream().filter(u -> u.getRegistrationDate() == null).collect(Collectors.toSet());
        changeRegistrationDate.forEach(u -> u.setRegistrationDate(new Date()));
        applicationFormRepository.save(changeRegistrationDate);
    }
}

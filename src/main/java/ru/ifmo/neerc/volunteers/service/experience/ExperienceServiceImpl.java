package ru.ifmo.neerc.volunteers.service.experience;

import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import ru.ifmo.neerc.dev.Pair;
import ru.ifmo.neerc.volunteers.entity.*;
import ru.ifmo.neerc.volunteers.repository.ApplicationFormRepository;
import ru.ifmo.neerc.volunteers.repository.MedalRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class ExperienceServiceImpl implements ExperienceService {

    final ApplicationFormRepository applicationFormRepository;
    final MedalRepository medalRepository;
    final MessageSource messageSource;
    private final Locale locale = LocaleContextHolder.getLocale();

    @Override
    public Map<ApplicationForm, Double> getExperiences(Year year) {
        int countEvents = year.getDays().size();
        Map<ApplicationForm, Double> expsCurYear = year.getUsers().stream().collect(
                Collectors.toMap(Function.identity(),
                        u -> u.getUserDays().stream().map(ud -> (ud.getAttendance() == Attendance.YES || ud.getAttendance() == Attendance.LATE) ? ud.getPosition().getValue() / countEvents : 0.0).reduce(0.0, (a, b) -> a + b)
                ));
        expsCurYear.forEach((u, exp) -> {
            if (exp != u.getExperience()) {
                u.setExperience(exp);
                applicationFormRepository.save(u);
            }
        });
        Map<ApplicationForm, Double> expExpYear = getExperienceExceptCurrentYear(year);
        Map<ApplicationForm, Double> totalExps = year.getUsers().stream().collect(
                Collectors.toMap(Function.identity(),
                        u -> expsCurYear.get(u) + expExpYear.get(u))
        );
        return totalExps;
    }

    @Override
    public Map<ApplicationForm, Double> getExperienceExceptCurrentYear(Year year) {
        return year.getUsers().stream().collect(
                Collectors.toMap(Function.identity(),
                        u -> u.getUser().getApplicationForms().stream()
                                .map(ApplicationForm::getExperience).reduce(0.0, (a, b) -> a + b) - u.getExperience())
        );
    }

    @Override
    public List<Pair<ApplicationForm, Medal>> getMedals(Year year) {
        List<ApplicationForm> applicationForms = new ArrayList<>(year.getUsers());
        Map<ApplicationForm, Double> exps = getExperienceExceptCurrentYear(year);
        applicationForms.sort((user1, user2) -> Double.compare(exps.get(user2), exps.get(user1)));
        List<Medal> medals = new ArrayList<>(medalRepository.findAll());
        medals.sort(Comparator.comparing(Medal::getValue).reversed());
        medals.add(new Medal(messageSource.getMessage("volunteers.results.noMedal", null, "No medals", locale), -1));
        final int[] j = new int[]{0};
        return applicationForms.stream().map(u -> {
            while (medals.get(j[0]).getValue() > exps.get(u)) {
                j[0]++;
            }
            return new Pair<>(u, medals.get(j[0]));
        }).collect(Collectors.toList());
    }


    @Override
    public Map<ApplicationForm, Set<Hall>> getHalls(Year year) {
        return year.getUsers().stream().collect(Collectors.toMap(Function.identity(),
                u -> u.getUserDays().stream().map(UserDay::getHall).collect(Collectors.toSet())
        ));
    }

    @Override
    public Map<ApplicationForm, Double> getAssessments(Year year) {
        Map<Attendance, String> attendanceString = getAttendanceMap();
        return year.getUsers().stream().collect(Collectors.toMap(Function.identity(),
                u -> u.getUserDays().stream().map(ud -> {
                    Set<Assessment> assessments = ud.getAssessments();
                    assessments.add(ud.createFakeAssessmentByAttendace(attendanceString.get(ud.getAttendance())));
                    return assessments.stream().map(Assessment::getValue).reduce(0.0, (a, b) -> a + b);
                }).reduce(0.0, (a, b) -> a + b)
        ));
    }

    @Override
    public List<Pair<ApplicationForm, Pair<Medal, Double>>> getResults(Year year) {
        List<ApplicationForm> applicationForms = new ArrayList<>(year.getUsers());
        Map<ApplicationForm, Double> exps = getExperienceExceptCurrentYear(year);
        Map<ApplicationForm, Double> asses = getAssessments(year);
        applicationForms.sort((user1, user2) -> Double.compare(exps.get(user2), exps.get(user1)));
        List<Medal> medals = new ArrayList<>(medalRepository.findAll());
        medals.add(new Medal(messageSource.getMessage("volunteers.results.noMedal", null, "No medals", locale), -1));
        medals.sort(Comparator.comparing(Medal::getValue).reversed());
        final int[] j = new int[0];
        return applicationForms.stream().map(u -> {
            while (medals.get(j[0]).getValue() > exps.get(u)) {
                j[0]++;
            }
            return new Pair<>(u, new Pair<>(medals.get(j[0]), asses.get(u)));
        }).collect(Collectors.toList());
    }

    private Map<Attendance, String> getAttendanceMap() {
        return new HashMap<>(Arrays.stream(Attendance.values())
                .collect(Collectors.toMap(Function.identity(), attendance -> messageSource.getMessage("volunteers.attendance." + attendance.name().toLowerCase(), null, attendance.name(), locale))));
    }
}

package ru.ifmo.neerc.volunteers.service.experience;

import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;
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
    public Map<ApplicationForm, Double> getExperienceExceptCurrentYear(Year year) {
        return year.getUsers().stream().collect(
                Collectors.toMap(Function.identity(),
                        u -> u.getUser().getApplicationForms().stream()
                                .mapToDouble(ApplicationForm::getExperience).sum() - u.getExperience())
        );
    }

    @Override
    public List<Pair<ApplicationForm, Medal>> getMedals(Year year) {
        List<ApplicationForm> applicationForms = new ArrayList<>(year.getUsers());
        Map<ApplicationForm, Double> exps = getExperienceExceptCurrentYear(year);
        applicationForms.sort((user1, user2) -> Double.compare(exps.get(user2), exps.get(user1)));
        List<Medal> medals = new ArrayList<>(medalRepository.findAll());
        medals.sort(Comparator.comparing(Medal::getValue).reversed());
        medals.add(new Medal(messageSource.getMessage("volunteers.results.noMedal", null, "No medals", locale), -1, 0));
        final int[] j = new int[]{0};
        return applicationForms.stream().map(u -> {
            while (medals.get(j[0]).getValue() > exps.get(u)) {
                j[0]++;
            }
            return new Pair<>(u, medals.get(j[0]));
        }).collect(Collectors.toList());
    }

    public List<ApplicationForm> getApplicationForms(Map<ApplicationForm, Double> experience, Map<ApplicationForm, Double> assessments) {
        final List<ApplicationForm> applicationForms = new ArrayList<>(experience.keySet());
        applicationForms.sort(
                (user1, user2) -> {
                    if (experience.get(user1).equals(experience.get(user2))) {
                        return Double.compare(assessments.get(user2), assessments.get(user1));
                    } else {
                        return Double.compare(experience.get(user2), experience.get(user1));
                    }
                }
        );
        return applicationForms;
    }

    public Pair<Map<ApplicationForm, Double>, Map<ApplicationForm, List<String>>> getAssessments(Year year) {
        final Map<ApplicationForm, Double> assessments = new HashMap<>();
        final Map<ApplicationForm, List<String>> assessmentsGroupByDays = new HashMap<>();
        Map<Attendance, String> attendanceComments = getAttendanceMap();
        for (ApplicationForm user : year.getUsers()) {
            final double[] assessment = {0.0};
            assessmentsGroupByDays.put(user, new ArrayList<>());
            for (UserDay userDay : user.getUserDays()) {
                Set<Assessment> allAssessments = new HashSet<>(userDay.getAssessments());
                allAssessments.add(userDay.createFakeAssessmentByAttendace(attendanceComments.get(userDay.getAttendance())));

                allAssessments.forEach(
                        userEventAssessment -> assessment[0] += userEventAssessment.getValue());

                String str = StringUtils.join(allAssessments.stream().map(Assessment::getValue).collect(Collectors.toList()), ", ");
                assessmentsGroupByDays.get(user).add("(" + str + ")");

            }
            assessments.put(user, assessment[0]);
        }
        return new Pair<>(assessments, assessmentsGroupByDays);
    }

    @Override
    public Map<ApplicationForm, Double> getExperience(Year year) {
        final Map<ApplicationForm, Double> experience = new HashMap<>();
        final Set<ApplicationForm> needToSave = new HashSet<>();
        final Map<ApplicationForm, Double> baseExp = getExperienceExceptCurrentYear(year);
        final double countEvents = year.getDays().stream().mapToDouble(Day::getAttendanceValue).sum();
        for (ApplicationForm user : year.getUsers()) {
            double totalExp = baseExp.get(user);
            double exp = 0;
            for (final UserDay userDay : user.getUserDays()) {
                if (userDay.getAttendance() == Attendance.YES || userDay.getAttendance() == Attendance.LATE) {
                    exp += userDay.getPosition().getValue() / countEvents;
                }
            }
            totalExp += exp;
            experience.put(user, totalExp);
            if (exp != user.getExperience()) {
                user.setExperience(exp);
                needToSave.add(user);
            }
        }
        applicationFormRepository.save(needToSave);
        return experience;
    }

    @Override
    public Map<ApplicationForm, Set<Hall>> getHalls(Year year) {
        return year.getUsers().stream().collect(Collectors.toMap(
                Function.identity(),
                user -> user.getUserDays().stream().map(UserDay::getHall).collect(Collectors.toSet())
        ));
    }

    @Override
    public Map<ApplicationForm, Medal> getNewMedals(List<ApplicationForm> applicationForms, Map<ApplicationForm, Double> experience) {
        final Map<ApplicationForm, Medal> userMedals = new HashMap<>();
        final List<Medal> medals = new ArrayList<>(medalRepository.findAll());
        medals.sort(Comparator.comparing(Medal::getValue).reversed());
        medals.add(new Medal(messageSource.getMessage("volunteers.results.noMedal", null, "No medal", locale), -1, 0));
        for (int i = 0, j = 0; i < applicationForms.size(); i++) {
            while (medals.get(j).getValue() > experience.get(applicationForms.get(i))) {
                j++;
            }
            userMedals.put(applicationForms.get(i), medals.get(j));
        }
        return userMedals;
    }


    private Map<Attendance, String> getAttendanceMap() {
        return new HashMap<>(Arrays.stream(Attendance.values())
                .collect(Collectors.toMap(Function.identity(), attendance -> messageSource.getMessage("volunteers.attendance." + attendance.name().toLowerCase(), null, attendance.name(), locale))));
    }
}

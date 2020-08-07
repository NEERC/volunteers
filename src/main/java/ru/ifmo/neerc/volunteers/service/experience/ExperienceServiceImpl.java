package ru.ifmo.neerc.volunteers.service.experience;

import lombok.AllArgsConstructor;
import org.decimal4j.util.DoubleRounder;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;
import org.thymeleaf.util.StringUtils;
import ru.ifmo.neerc.volunteers.entity.*;
import ru.ifmo.neerc.volunteers.repository.ApplicationFormRepository;
import ru.ifmo.neerc.volunteers.repository.AssBoundaryRepository;
import ru.ifmo.neerc.volunteers.repository.MedalRepository;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@AllArgsConstructor
public class ExperienceServiceImpl implements ExperienceService {

    final ApplicationFormRepository applicationFormRepository;
    final MedalRepository medalRepository;
    final MessageSource messageSource;
    final AssBoundaryRepository assBoundaryRepository;
    private final Locale locale = LocaleContextHolder.getLocale();


    @Override
    public Map<ApplicationForm, Double> getExperienceExceptCurrentYear(Year year) {
        return year.getUsers().stream().collect(
                Collectors.toMap(Function.identity(),
                        u -> getExperienceExceptCurrentYear(u))
        );
    }

    private Double getExperienceExceptCurrentYear(ApplicationForm user) {
        return user.getUser().getApplicationForms().stream()
                .mapToDouble(ApplicationForm::getExperience).sum() - user.getExperience();
    }

    @Override
    public List<Pair<ApplicationForm, Medal>> getMedals(Year year) {
        List<ApplicationForm> applicationForms = new ArrayList<>(year.getUsers());
        Map<ApplicationForm, Double> exps = getExperienceExceptCurrentYear(year);
        applicationForms.sort((user1, user2) -> Double.compare(exps.get(user2), exps.get(user1)));
        List<Medal> medals = new ArrayList<>(medalRepository.findAll());
        medals.sort(Comparator.comparing(Medal::getValue).reversed());
        medals.add(new Medal(messageSource.getMessage("volunteers.results.noMedal", null, "No medals", locale),
                messageSource.getMessage("volunteers.results.noMedal.cur", null, "No medals", locale),
                -1, 0));
        final int[] j = new int[]{0};
        return applicationForms.stream().map(u -> {
            while (medals.get(j[0]).getValue() > exps.get(u)) {
                j[0]++;
            }
            return Pair.of(u, medals.get(j[0]));
        }).collect(Collectors.toList());
    }

    @Override
    public List<ApplicationForm> getSortedApplicationForms(Map<ApplicationForm, Double> assessments,
                                                           Map<ApplicationForm, Medal> medals) {

        NavigableSet<Double> borders = assessments.keySet().stream().findAny()
                .map(f -> assBoundaryRepository.findByYear(f.getYear())).orElseGet(Collections::emptyList).stream().map(AssBoundary::getValue)
                .collect(Collectors.toCollection(TreeSet::new));
        borders.add(Double.NEGATIVE_INFINITY);

        return assessments.entrySet().stream().filter(e -> !e.getKey().getUser().isHq())
                .sorted(Comparator.<Map.Entry<ApplicationForm, Double>>comparingLong(e -> medals.get(e.getKey()).getValue())
                        .thenComparing(e -> borders.floor(e.getValue()))
                        .thenComparing(e -> e.getKey().getUser().getLastNameCyr())
                )
                .map(Map.Entry::getKey).collect(Collectors.toList());
    }

    public Pair<Map<ApplicationForm, Double>, Map<ApplicationForm, List<String>>> getAssessments(Year year) {
        final Map<ApplicationForm, Double> assessments = new HashMap<>();
        final Map<ApplicationForm, List<String>> assessmentsGroupByDays = new HashMap<>();
        Map<Attendance, String> attendanceComments = getAttendanceMap();
        for (ApplicationForm user : year.getUsers()) {
            if (user.getUser().isHq()) {
                continue;
            }
            double assessment = 0.0;
            assessmentsGroupByDays.put(user, new ArrayList<>());
            for (UserDay userDay : user.getUserDays()) {
                Set<Assessment> allAssessments = new HashSet<>(userDay.getAssessments());
                allAssessments.add(userDay.createFakeAssessmentByAttendace(attendanceComments));

                assessment += allAssessments.stream().mapToDouble(Assessment::getValue).sum();

                String str = StringUtils.join(allAssessments.stream().map(Assessment::getValue).collect(Collectors.toList()), ", ");
                assessmentsGroupByDays.get(user).add("(" + str + ")");

            }
            assessments.put(user, assessment);
        }
        return Pair.of(assessments, assessmentsGroupByDays);
    }

    @Override
    public Map<ApplicationForm, Double> getExperience(Year year) {
        final Map<ApplicationForm, Double> experience = new HashMap<>();
        final Set<ApplicationForm> needToSave = new HashSet<>();
        final double countEvents = year.getDays().stream().mapToDouble(Day::getAttendanceValue).sum();
        for (ApplicationForm user : year.getUsers()) {
            experience.put(user, getExperience(year, user, countEvents));
        }
        applicationFormRepository.save(needToSave);
        return experience;
    }

    @Override
    public Double getExperience(Year year, ApplicationForm user) {
        return getExperience(year, user, year.getDays().stream().mapToDouble(Day::getAttendanceValue).sum());
    }

    private Double getExperience(Year year, ApplicationForm user, final double countEvents) {
        double totalExp = getExperienceExceptCurrentYear(user);
        double exp = 0;
        for (final UserDay userDay : user.getUserDays()) {
            if (userDay.getAttendance() == Attendance.YES || userDay.getAttendance() == Attendance.LATE) {
                exp += userDay.getPosition().getValue() * userDay.getDay().getAttendanceValue();
            }
        }
        exp /= countEvents;
        exp = DoubleRounder.round(exp, 2);
        exp += user.getExtraExperience();
        totalExp += exp;

        if (exp != user.getExperience()) {
            user.setExperience(exp);
            applicationFormRepository.save(user);
        }
        return totalExp;
    }

    @Override
    public Map<ApplicationForm, Set<Hall>> getHalls(Year year) {
        return year.getUsers().stream().collect(Collectors.toMap(
                Function.identity(),
                user -> user.getUserDays().stream().map(UserDay::getHall).collect(Collectors.toSet())
        ));
    }

    @Override
    public Map<ApplicationForm, Medal> getNewMedals(Map<ApplicationForm, Double> experience) {
        String noMedalEng = messageSource.getMessage("volunteers.results.noMedal", null, "No medal", locale);
        String noMedalCyr = messageSource.getMessage("volunteers.results.noMedal.cur", null, "Без медали", locale);
        Medal noMedal = new Medal(noMedalEng, noMedalCyr, -1, 0);

        TreeMap<Long, Medal> medals = Stream.concat(medalRepository.findAll().stream(), Stream.of(noMedal))
                .collect(Collectors.toMap(Medal::getValue, Function.identity(), (a, b) -> a, TreeMap::new));
        return experience.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey,
                v -> medals.floorEntry(v.getValue().longValue()).getValue()));
    }


    private Map<Attendance, String> getAttendanceMap() {
        return new HashMap<>(Arrays.stream(Attendance.values())
                .collect(Collectors.toMap(Function.identity(), attendance -> messageSource.getMessage("volunteers.attendance." + attendance.name().toLowerCase(), null, attendance.name(), locale))));
    }
}

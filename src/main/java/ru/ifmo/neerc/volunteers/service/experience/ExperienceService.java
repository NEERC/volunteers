package ru.ifmo.neerc.volunteers.service.experience;

import org.springframework.data.util.Pair;
import ru.ifmo.neerc.volunteers.entity.ApplicationForm;
import ru.ifmo.neerc.volunteers.entity.Hall;
import ru.ifmo.neerc.volunteers.entity.Medal;
import ru.ifmo.neerc.volunteers.entity.Year;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ExperienceService {

    Map<ApplicationForm, Double> getExperienceExceptCurrentYear(Year year);

    List<Pair<ApplicationForm, Medal>> getMedals(Year year);

    Map<ApplicationForm, Set<Hall>> getHalls(Year year);

    Map<ApplicationForm, Double> getExperience(Year year);

    Pair<Map<ApplicationForm, Double>, Map<ApplicationForm, List<String>>> getAssessments(Year year);

    List<ApplicationForm> getSortedApplicationForms(Map<ApplicationForm, Double> assessments, Map<ApplicationForm, Medal> medals);

    Map<ApplicationForm, Medal> getNewMedals(Map<ApplicationForm, Double> experience);
}

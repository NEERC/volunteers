package ru.ifmo.neerc.volunteers.service.experience;

import ru.ifmo.neerc.dev.Pair;
import ru.ifmo.neerc.volunteers.entity.ApplicationForm;
import ru.ifmo.neerc.volunteers.entity.Hall;
import ru.ifmo.neerc.volunteers.entity.Medal;
import ru.ifmo.neerc.volunteers.entity.Year;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ExperienceService {
    Map<ApplicationForm, Double> getExperiences(Year year);

    Map<ApplicationForm, Double> getExperienceExceptCurrentYear(Year year);

    List<Pair<ApplicationForm, Medal>> getMedals(Year year);

    Map<ApplicationForm, Set<Hall>> getHalls(Year year);

    Map<ApplicationForm, Double> getAssessments(Year year);

    List<Pair<ApplicationForm, Pair<Medal, Double>>> getResults(Year year);

}

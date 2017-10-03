package ru.ifmo.neerc.volunteers.service.calendar;

import lombok.Data;

import java.util.Map;

/**
 * Created by Lapenok Akesej on 04.08.2017.
 */
@Data
public class Calendar {
    Map<String, Map<String, String>> events;
}

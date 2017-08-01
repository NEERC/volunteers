package ru.ifmo.neerc.volunteers.service;

import ru.ifmo.neerc.volunteers.entity.Event;

import java.io.IOException;

/**
 * Created by Lapenok Akesej on 29.07.2017.
 */
public interface CalendarService {
    void init() throws IOException;

    String addEvent(Event event) throws IOException;

    void updateEvent(Event event) throws IOException;
}

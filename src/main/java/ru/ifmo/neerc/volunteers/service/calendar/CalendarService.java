package ru.ifmo.neerc.volunteers.service.calendar;

/**
 * Created by Lapenok Akesej on 29.07.2017.
 */
public interface CalendarService {
    String getCalendars(final long id);

    String getCalendar(final long id, String calendarName);
}

package ru.ifmo.neerc.volunteers.controller;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.ifmo.neerc.volunteers.service.calendar.CalendarService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by Lapenok Akesej on 03.08.2017.
 */
@RequestMapping("/api/calendar/")
@Controller
@AllArgsConstructor
public class CalendarController {
    private final CalendarService calendarService;

    @GetMapping(value = "{id}")
    public void getCalendars(@PathVariable final long id, HttpServletResponse response) throws IOException {
        sendCalendar(response, calendarService.getCalendars(id));
    }

    @GetMapping("{id}/{calendarName}")
    public void getCalendar(@PathVariable final long id, @PathVariable final String calendarName, HttpServletResponse response) throws IOException {
        sendCalendar(response, calendarService.getCalendar(id, calendarName));
    }

    private void sendCalendar(HttpServletResponse response, String calendar) throws IOException {
        response.setContentType("data:text/ics;charset=utf-8");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"calendar.ics\"");
        PrintWriter writer = response.getWriter();
        writer.print(calendar);
        writer.flush();
        writer.close();
    }
}

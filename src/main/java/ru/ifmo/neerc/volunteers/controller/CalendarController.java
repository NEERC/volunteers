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
        response.setContentType("text/html; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        PrintWriter writer = response.getWriter();
        writer.println(calendarService.getCalendars(id));
        response.setHeader("Content-Disposition", "attachment; filename=\"calendar.ics\"");
        response.flushBuffer();
    }

    @GetMapping("{id}/{calendarName}")
    public void getCalendar(@PathVariable final long id, @PathVariable final String calendarName, HttpServletResponse response) throws IOException {
        response.getOutputStream().print(calendarService.getCalendar(id, calendarName));
        response.setContentType("data:text/ics;charset=utf-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"calendar.ics\"");
        response.flushBuffer();
    }
}

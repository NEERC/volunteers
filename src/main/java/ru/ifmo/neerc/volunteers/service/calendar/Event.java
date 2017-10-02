package ru.ifmo.neerc.volunteers.service.calendar;

import lombok.Data;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;


/**
 * Created by Lapenok Akesej on 04.08.2017.
 */
@Data
public class Event {

    private static final SimpleDateFormat parserWithTime = new SimpleDateFormat("yyyy-MM-dd HH:mm");
    private static final SimpleDateFormat parserWithoutTime = new SimpleDateFormat("yyyy-MM-dd");

    String name;
    String location;
    String description;
    Date start;
    boolean withTime;
    Date end;

    public Event(Map<String, String> data) throws ParseException {
        name = data.get("name");
        location = data.get("location");
        description = data.get("description");
        try {
            start = parserWithTime.parse(data.get("start"));
            withTime = true;
        } catch (ParseException e) {
            start = parserWithoutTime.parse(data.get("start"));
            withTime = false;
        }
        if (withTime) {
            end = parserWithTime.parse(data.get("end"));
        }
    }
}

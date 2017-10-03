package ru.ifmo.neerc.volunteers.service.calendar;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.StandardTime;
import biweekly.component.VEvent;
import biweekly.component.VTimezone;
import biweekly.io.TimezoneAssignment;
import biweekly.property.Method;
import biweekly.property.RawProperty;
import biweekly.util.UtcOffset;
import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.ifmo.neerc.volunteers.repository.YearRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

/**
 * Created by Lapenok Akesej on 29.07.2017.
 */
@Service
@AllArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private final YearRepository yearRepository;
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String getCalendars(final long id) {
        List<ICalendar> iCalendars = new ArrayList<>();
        try {
            Map<String, List<Map<String, String>>> calendars = readYaml(id);
            for (String calendarName : calendars.keySet()) {
                //logger.info("Generate calendar " + calendarName);
                List<Map<String, String>> calendar = calendars.get(calendarName);
                iCalendars.add(generateCalendar(calendar, calendarName));
            }
        } catch (Exception e) {
            logger.error("Error, while try find and parse file: calendar" + id, e);
        }
        return Biweekly.write(iCalendars).go();
    }

    @Override
    public String getCalendar(final long id, String calendarName) {
        ICalendar iCalendar = null;
        try {
            //logger.info("Generate calendar " + calendarName);
            Map<String, List<Map<String, String>>> calendars = readYaml(id);
            iCalendar = generateCalendar(calendars.get(calendarName), calendarName);
        } catch (Exception e) {
            logger.error("Error, while try find and parse file: calendar" + id, e);
        }
        if (iCalendar == null) {
            iCalendar = new ICalendar();
            iCalendar.setProductId(calendarName);
        }
        return Biweekly.write(iCalendar).go();
    }

    private ICalendar generateCalendar(List<Map<String, String>> calendar, String name) {
        ICalendar iCalendar = new ICalendar();
        iCalendar.setProductId("neerc.ifmo.ru/volunteers/");
        iCalendar.setName(name);
        iCalendar.setMethod(Method.PUBLISH);
        iCalendar.addProperty(new RawProperty("X-WR-CALNAME", name));
        iCalendar.addProperty(new RawProperty("X-WR-TIMEZONE", "Europe/Moscow"));
        //iCalendar.getTimezoneInfo().setDefaultTimezone(new TimezoneAssignment(TimeZone.getDefault(), "Europe/Moscow"));
        iCalendar.addCategories();
        if (calendar != null) {
            for (Map<String, String> eventMap : calendar) {
                //logger.info("Generate event " + eventSummery);
                try {
                    Event event = new Event(eventMap);
                    VEvent vEvent = new VEvent();

                    vEvent.setDateStart(event.getStart(), event.isWithTime());

                    if (event.isWithTime())
                        vEvent.setDateEnd(event.getEnd());

                    vEvent.setDescription(event.getDescription());

                    vEvent.setLocation(event.getLocation());

                    vEvent.setSummary(event.getName());

                    iCalendar.addEvent(vEvent);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        logger.info("download timezoneAssignment");
        VTimezone timezone = new VTimezone("Europe/Moscow");
        timezone.setTimezoneUrl("http://tzurl.org/zoneinfo-outlook/Europe/Moscow");
        timezone.addExperimentalProperty("X-LIC-LOCATION", "Europe/Moscow");
        StandardTime standardTime = new StandardTime();
        standardTime.setTimezoneOffsetFrom(new UtcOffset(true, 3, 0));
        standardTime.setTimezoneOffsetTo(new UtcOffset(true, 3, 0));
        standardTime.addExperimentalProperty("TZNAME", "MSK");
        standardTime.addExperimentalProperty("DTSTART", "19700101T000000");
        timezone.addStandardTime(standardTime);
        TimezoneAssignment current = new TimezoneAssignment(TimeZone.getDefault(), timezone);
        iCalendar.getTimezoneInfo().setDefaultTimezone(current);
        logger.info("downloaded");
        return iCalendar;
    }

    private Map<String, List<Map<String, String>>> readYaml(final long id) throws YamlException {
        //logger.info("Parse yaml file for year" + id);
        YamlReader reader = new YamlReader(yearRepository.findOne(id).getCalendar());
        Object result = reader.read();
        if (result instanceof Map) {
            return (Map<String, List<Map<String, String>>>) result;
        } else
            throw new YamlException("Result is not a map");
    }


}

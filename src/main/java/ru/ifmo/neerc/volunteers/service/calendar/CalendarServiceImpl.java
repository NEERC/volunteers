package ru.ifmo.neerc.volunteers.service.calendar;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import biweekly.io.TimezoneAssignment;
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
            Map<String, Map<String, Map<String, String>>> calendars = readYaml(id);
            for (String calendarName : calendars.keySet()) {
                //logger.info("Generate calendar " + calendarName);
                Map<String, Map<String, String>> calendar = calendars.get(calendarName);
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
            Map<String, Map<String, Map<String, String>>> calendars = readYaml(id);
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

    private ICalendar generateCalendar(Map<String, Map<String, String>> calendar, String name) {
        ICalendar iCalendar = new ICalendar();
        iCalendar.setProductId(name);
        iCalendar.setName(name);
        iCalendar.getTimezoneInfo().setDefaultTimezone(new TimezoneAssignment(TimeZone.getDefault(), TimeZone.getDefault().getID()));
        if (calendar != null) {
            for (String eventSummery : calendar.keySet()) {
                //logger.info("Generate event " + eventSummery);
                try {
                    Event event = new Event(calendar.get(eventSummery));
                    VEvent vEvent = new VEvent();

                    vEvent.setDateStart(event.getStart(), event.isWithTime());

                    if (event.isWithTime())
                        vEvent.setDateEnd(event.getEnd());

                    vEvent.setDescription(event.getDescription());

                    vEvent.setLocation(event.getLocation());

                    vEvent.setSummary(eventSummery);

                    iCalendar.addEvent(vEvent);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return iCalendar;
    }

    private Map<String, Map<String, Map<String, String>>> readYaml(final long id) throws YamlException {
        //logger.info("Parse yaml file for year" + id);
        YamlReader reader = new YamlReader(yearRepository.findOne(id).getCalendar());
        Object result = reader.read();
        if (result instanceof Map) {
            return (Map<String, Map<String, Map<String, String>>>) result;
        } else
            throw new YamlException("Result is not a map");
    }


}

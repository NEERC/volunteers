package ru.ifmo.neerc.volunteers.service.calendar;

import biweekly.Biweekly;
import biweekly.ICalendar;
import biweekly.component.VEvent;
import com.esotericsoftware.yamlbeans.YamlReader;
import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import ru.ifmo.neerc.volunteers.entity.Year;

import java.io.FileReader;
import java.util.TimeZone;

/**
 * Created by Lapenok Akesej on 29.07.2017.
 */
@Service
@AllArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public String getCalendar(Year year) {
        ICalendar iCalendar = new ICalendar();
        try {
            YamlReader reader = new YamlReader(new FileReader("calendar" + year.getId()));
            Calendar calendar = reader.read(Calendar.class);
            for (String name : calendar.getEvents().keySet()) {
                Event event = new Event(calendar.events.get(name));
                VEvent vEvent = new VEvent();

                vEvent.setDateStart(event.getStart());
                vEvent.getDateStart().setTimezoneId(TimeZone.getDefault().getID());

                vEvent.setDateEnd(event.getEnd());
                vEvent.getDateEnd().setTimezoneId(TimeZone.getDefault().getID());

                vEvent.setDescription(event.getDescription());

                vEvent.setSummary(event.getSummery());

                iCalendar.addEvent(vEvent);
            }
        } catch (Exception e) {
            logger.error("Error, while try find and parse file: calendar" + year.getId(), e);
        }
        iCalendar.setProductId(year.getName());
        Biweekly.WriterChainTextSingle text = Biweekly.write(iCalendar);
        return text.go();
    }
}

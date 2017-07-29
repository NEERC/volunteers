package ru.ifmo.neerc.volunteers.service;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.calendar.CalendarScopes;
import com.google.api.services.calendar.model.Calendar;
import com.google.api.services.calendar.model.CalendarList;
import com.google.api.services.calendar.model.CalendarListEntry;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Lapenok Akesej on 29.07.2017.
 */
@Service
@Component
@RequiredArgsConstructor
public class CalendarServiceImpl implements CalendarService {

    private static final String APPLICATION_NAME = "Volunteers";

    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    private static final File DATA_STORE_DIR = new File(".credentials/calendar-volunteers");

    private static HttpTransport HTTP_TRANSPORT;

    private static FileDataStoreFactory DATA_STORE_FACTORY;

    private static final List<String> SCOPES = Arrays.asList(CalendarScopes.CALENDAR);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }
    }

    public static Credential authorize() throws IOException {
        InputStream in = CalendarServiceImpl.class.getResourceAsStream("/client_secret.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(DATA_STORE_FACTORY)
                .setAccessType("offline")
                .build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver()).authorize("user");
        return credential;
    }

    private final Map<ru.ifmo.neerc.volunteers.entity.Calendar, Calendar> calendarMap = new HashMap<>();
    private final MessageSource messageSource;
    private final Locale locale = LocaleContextHolder.getLocale();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private Map<ru.ifmo.neerc.volunteers.entity.Calendar, String> names;

    //@PostConstruct
    @Override
    public void init() throws IOException {
        //Initialize calendar with valid OAuth credentials
        com.google.api.services.calendar.Calendar service = new com.google.api.services.calendar.Calendar.Builder(HTTP_TRANSPORT, JSON_FACTORY, authorize())
                .setApplicationName(APPLICATION_NAME).build();

        names = Arrays.stream(ru.ifmo.neerc.volunteers.entity.Calendar.values()).collect(Collectors.toMap(Function.identity(), calendar -> messageSource.getMessage("volunteers.calendar." + calendar.name().toLowerCase() + ".name", null, locale)));

        String pageToken = null;
        do {
            CalendarList calendarList = service.calendarList().list().setPageToken(pageToken).execute();
            List<CalendarListEntry> items = calendarList.getItems();
            for (CalendarListEntry calendarListEntry : items) {
                for (ru.ifmo.neerc.volunteers.entity.Calendar calendar : ru.ifmo.neerc.volunteers.entity.Calendar.values()) {
                    if (names.get(calendar).equals(calendarListEntry.getSummary())) {
                        calendarMap.put(calendar, service.calendars().get(calendarListEntry.getId()).execute());
                        break;
                    }
                }
            }
            pageToken = calendarList.getNextPageToken();
        } while (pageToken != null && calendarMap.size() == names.size());

        for (ru.ifmo.neerc.volunteers.entity.Calendar calendar : ru.ifmo.neerc.volunteers.entity.Calendar.values()) {
            if (!calendarMap.containsKey(calendar)) {
                Calendar calendar1 = new Calendar();
                calendar1.setSummary(calendar.name());
                calendar1.setTimeZone(TimeZone.getDefault().getID());
                Calendar createdCalendar = service.calendars().insert(calendar1).execute();
                calendarMap.put(calendar, createdCalendar);
            }
        }
        logger.info("Google initialize done");
    }
}

package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import lombok.ToString;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by Lapenok Akesej on 01.08.2017.
 */
@Data
@Entity
@ToString(exclude = {"id", "isSaved", "isChanged", "isDeleted"})
public class Event {

    @Id
    @GeneratedValue
    long id;

    @Enumerated(EnumType.STRING)
    Calendar calendar;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    Date start;

    @Temporal(TemporalType.DATE)
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm")
    Date end;

    String summery;

    String location;

    boolean isSaved = false;

    boolean isChanged = false;

    boolean isDeleted = false;

    String eventId = "";
}

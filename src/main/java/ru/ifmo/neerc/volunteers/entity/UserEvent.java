package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Lapenok Akesej on 26.02.2017.
 */
@Entity
@Data
@ToString(exclude = {"event","id"})
public class UserEvent {
    @Id
    @GeneratedValue
    long id;

    @JoinColumn(name = "userYear")
    @ManyToOne
    ApplicationForm userYear;

    @JoinColumn(name = "event")
    @ManyToOne
    Event event;

    @ManyToOne
    @JoinColumn(name = "position")
    PositionValue position;

    @ManyToMany(fetch = FetchType.LAZY)
    Set<UserEventAssessment> assessments;

    @ManyToOne
    Hall hall;

    @Enumerated(EnumType.STRING)
    Attendance attendance;
}

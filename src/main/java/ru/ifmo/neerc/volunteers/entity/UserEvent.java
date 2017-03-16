package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;

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
    @ManyToOne(fetch = FetchType.LAZY)
    ApplicationForm userYear;

    @JoinColumn(name = "event")
    @ManyToOne(fetch = FetchType.LAZY)
    Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position")
    Position position;

    @OneToMany(fetch = FetchType.LAZY)
    List<UserEventAssessment> assessments;

    @ManyToOne
    Hall hall;

    @Enumerated(EnumType.STRING)
    Attendance attendance;
}

package ru.ifmo.neerc.volunteers.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.Set;

/**
 * Created by Lapenok Akesej on 26.02.2017.
 */
@Entity
@Data
@ToString(exclude = {"event", "id", "assessments"})
@EqualsAndHashCode(exclude = {"assessments"})
@JsonIgnoreProperties(value = {"event", "userYear"})
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

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "user")
    Set<Assessment> assessments;

    @ManyToOne
    Hall hall;

    @Enumerated(EnumType.STRING)
    Attendance attendance = Attendance.UNKNOWN;
}

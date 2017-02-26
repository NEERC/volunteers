package com.volunteer.home.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;

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
    UserYear userYear;

    @JoinColumn(name = "event")
    @ManyToOne(fetch = FetchType.LAZY)
    Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "position")
    Position position;

    @ManyToOne
    Hall hall;
}

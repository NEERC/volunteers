package ru.ifmo.neerc.volunteers.entity;

import javax.persistence.*;

/**
 * Created by Lapenok Akesej on 08.03.2017.
 */
@Entity
public class PositionValue {
    @Id
    @GeneratedValue
    long id;

    @ManyToOne
    Position position;

    int value;

    @ManyToOne
    Year year;
}

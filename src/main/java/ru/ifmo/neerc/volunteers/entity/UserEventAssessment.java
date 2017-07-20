package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

/**
 * Created by Lapenok Akesej on 08.03.2017.
 */
@Entity
@Data
public class UserEventAssessment {

    @Id
    @GeneratedValue
    int id;

    @NotEmpty
    String comment;

    double value;

    @ManyToOne
    Event event;
}

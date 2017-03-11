package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by Lapenok Akesej on 08.03.2017.
 */
@Data
@Entity
public class Medal {

    @Id
    @GeneratedValue
    int id;

    @NotEmpty
    String name;

    int value;
}

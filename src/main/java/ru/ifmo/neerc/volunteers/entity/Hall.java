package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by Lapenok Akesej on 26.02.2017.
 */
@Entity
@Data
@ToString(exclude = {"id"})
public class Hall {

    @Id
    @GeneratedValue
    long id;

    @NotEmpty(message = "Надо указать имя")
    String name;
}
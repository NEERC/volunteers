package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

/**
 * Created by Lapenok Akesej on 26.02.2017.
 */
@Entity
@Data
public class Position {

    @Id
    @GeneratedValue
    private long id;

    @NotNull
    @NotEmpty(message = "надо указать имя")
    private String name;

    @NotNull
    private boolean def=false;
}

package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import ru.ifmo.neerc.volunteers.form.PositionForm;

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

    private String name;

    @NotNull
    private boolean def=false;

    public Position() {

    }

    public Position(PositionForm name) {
        this.name=name.getName();

    }
}

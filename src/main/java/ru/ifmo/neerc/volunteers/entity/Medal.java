package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Created by Lapenok Akesej on 08.03.2017.
 */
@Data
@Entity
@NoArgsConstructor
public class Medal {

    @Id
    @GeneratedValue
    private long id;

    @NotEmpty
    private String name;

    private int value;

    public Medal(String name, int value) {
        this.name = name;
        this.value = value;
    }
}

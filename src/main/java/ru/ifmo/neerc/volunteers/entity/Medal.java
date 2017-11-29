package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;
import ru.ifmo.neerc.volunteers.form.MedalForm;

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

    private long value;

    private long stars;

    public Medal(String name, long value, long stars) {
        this.name = name;
        this.value = value;
        this.stars = stars;
    }

    public Medal(MedalForm form) {
        name = form.getName();
        value = form.getValue();
        stars = form.getStars();
    }
}

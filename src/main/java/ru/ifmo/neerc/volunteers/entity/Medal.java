package ru.ifmo.neerc.volunteers.entity;

import lombok.AllArgsConstructor;
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
@AllArgsConstructor
public class Medal {

    @Id
    @GeneratedValue
    private long id;

    @NotEmpty
    private String name;

    @NotEmpty
    private String curName;

    private long value;

    private long stars;

    public Medal(String name, String curName, long value, long stars) {
        this.name = name;
        this.curName = curName;
        this.value = value;
        this.stars = stars;
    }

    public Medal(MedalForm form) {
        this(form.getName(), form.getCurName(), form.getValue(), form.getStars());
    }

    public void setFields(MedalForm form) {
        setName(form.getName());
        setCurName(form.getCurName());
        setValue(form.getValue());
        setStars(form.getStars());
    }
}

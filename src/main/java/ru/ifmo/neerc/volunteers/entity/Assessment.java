package ru.ifmo.neerc.volunteers.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(value = {"day", "user"})
public class Assessment {

    @Id
    @GeneratedValue
    int id;

    @NotEmpty
    String comment;

    double value;

    @ManyToOne
    UserDay user;

    @ManyToOne
    User author;
}

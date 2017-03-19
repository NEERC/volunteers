package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Set;

/**
 * Created by Lapenok Akesej on 25.02.2017.
 */
@Data
@Entity
@EqualsAndHashCode(exclude = {"year","users"})
@ToString(exclude = {"year","users"})
public class Event {

    @Id
    @GeneratedValue
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year")
    private Year year;

    @NotNull
    @NotEmpty
    private String name;

    @NotNull
    private String information;

    @NotNull
    private int attendanceValue;

    @OneToMany(fetch = FetchType.LAZY)
    Set<UserEvent> users;
}

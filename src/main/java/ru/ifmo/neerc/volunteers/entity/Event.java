package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Lapenok Akesej on 25.02.2017.
 */
@Data
@Entity
@EqualsAndHashCode(exclude = {"year", "users", "assessments"})
@ToString(exclude = {"year", "users", "assessments"})
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
    private double attendanceValue;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "event")
    @OrderBy("id ASC")
    Set<UserEvent> users;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "event")
    @OrderBy("id ASC")
    Set<Assessment> assessments;

    public void addUser(final UserEvent ue) {
        if (ue != null) {
            getUsers();
            if (users == null) {
                users = new HashSet<>();
            }
            users.add(ue);
            ue.setEvent(this);
        }
    }
}

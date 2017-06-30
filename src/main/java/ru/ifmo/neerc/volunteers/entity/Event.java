package ru.ifmo.neerc.volunteers.entity;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.validation.constraints.NotNull;

import org.hibernate.validator.constraints.NotEmpty;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
    private int attendanceValue;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "event")
    @OrderBy("id ASC")
    Set<UserEvent> users;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "event")
    @OrderBy("id ASC")
    Set<UserEventAssessment> assessments;

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

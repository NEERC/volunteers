package com.volunteer.home.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

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
    private String name;

    @NotNull
    private String information;

    @OneToMany(fetch = FetchType.LAZY)
    Set<UserEvent> users;
}

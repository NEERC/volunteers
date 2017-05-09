package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import lombok.ToString;

import java.util.Set;

import javax.persistence.*;

import org.hibernate.validator.constraints.NotEmpty;

/**
 * Created by Lapenok Akesej on 26.02.2017.
 */
@Entity
@Data
@ToString(exclude = {"id","year"}, includeFieldNames = false)
public class ApplicationForm {

    @Id
    @GeneratedValue
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year")
    private Year year;

    @NotEmpty
    @ManyToMany(fetch = FetchType.LAZY)
    private Set<Position> position;

    private String suggestions;

    @NotEmpty
    @Column(name = "`group`")
    private String group;
}

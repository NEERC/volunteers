package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import javax.persistence.*;
import java.util.List;
import java.util.Set;

/**
 * Created by Lapenok Akesej on 26.02.2017.
 */
@Entity
@Data
@ToString(exclude = {"id", "year", "userDays"}, includeFieldNames = false)
@EqualsAndHashCode(exclude = {"id", "year", "userDays"})
public class ApplicationForm {

    @Id
    @GeneratedValue
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "year")
    private Year year;

    @ManyToMany(fetch = FetchType.LAZY)
    private Set<PositionValue> positions;

    private String suggestions;

    @Column(name = "`group`")
    private String group;

    private double experience;

    @OneToMany(mappedBy = "userYear")
    @OrderBy("event ASC")
    private List<UserDay> userDays;
}

package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Set;

/**
 * Created by Lapenok Akesej on 25.02.2017.
 */
@Data
@Entity
@EqualsAndHashCode(exclude = {"users","positionValues"})
@ToString(exclude = {"users","positionValues"})
public class Year {

    @Id
    @GeneratedValue
    private long id;

    @NotEmpty(message = "Надо указать название сезона")
    @Size(max = 255)
    private String name;

   /* private boolean open;

    private boolean current;*/

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "year")
    @OrderBy("id ASC")
    private Set<Event> events;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "year")
    @OrderBy(value = "id ASC ")
    private Set<ApplicationForm> users;

    @OneToMany(fetch = FetchType.EAGER, cascade = CascadeType.ALL, mappedBy = "year")
    @OrderBy(value = "name ASC")
    private Set<Hall> halls;

    @OneToMany(mappedBy = "year")
    private Set<PositionValue> positionValues;

    public Year(String name) {
        this.name = name;
    }

    public Year() {}
}

package ru.ifmo.neerc.volunteers.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
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
@JsonIgnoreType
@Table(indexes = {@Index(columnList = "name", unique = true)})
@NoArgsConstructor
public class Year {

    @Id
    @GeneratedValue
    private long id;

    @NotEmpty(message = "Надо указать название сезона")
    @Size(max = 255)
    private String name;

    private boolean openForRegistration;

    /*private boolean current;*/

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "year")
    @OrderBy("name ASC")
    private Set<Day> days;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "year")
    @OrderBy(value = "registrationDate ASC, id ASC ")
    private Set<ApplicationForm> users;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL, mappedBy = "year")
    @OrderBy(value = "name ASC")
    private Set<Hall> halls;

    @OneToMany(mappedBy = "year", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @OrderBy(value = "id ASC")
    private Set<PositionValue> positionValues;

    @Lob
    private String calendar = "";

    public Year(String name) {
        this.name = name;
    }

}

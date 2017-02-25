package com.volunteer.home.entity;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.Size;
import java.util.Set;

/**
 * Created by Lapenok Akesej on 25.02.2017.
 */
@Data
@Entity
public class Year {

    @Id
    @GeneratedValue
    private long id;

    @NotEmpty(message = "Надо указать название сезона")
    @Size(max = 255)
    private String name;

    private boolean open;

    private boolean current;

    @OneToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL,mappedBy = "year")
    @OrderBy("name ASC")
    private Set<Event> events;
}

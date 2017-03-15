package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;

/**
 * Created by Lapenok Akesej on 26.02.2017.
 */
@Entity
@Data
@ToString(exclude = {"id","year"})
@EqualsAndHashCode(exclude = {"year"})
public class Hall {

    @Id
    @GeneratedValue
    long id;

    @NotEmpty
    String name;

    @NotEmpty
    String decryption;

    @ManyToOne(fetch = FetchType.LAZY)
    Year year;
}

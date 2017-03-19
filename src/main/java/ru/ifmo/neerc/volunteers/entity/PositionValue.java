package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;

import javax.persistence.*;

/**
 * Created by Lapenok Akesej on 08.03.2017.
 */
@Entity
@Data
public class PositionValue {
    @Id
    @GeneratedValue
    long id;

    @ManyToOne
    Position position;

    int value;

    @ManyToOne
    Year year;

    public PositionValue() {

    }

    public PositionValue(Position position, Year year, int value){
        this.position=position;
        this.year=year;
        this.value=value;
    }
}

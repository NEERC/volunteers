package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import lombok.ToString;

import javax.persistence.*;

/**
 * Created by Matvey on 12/10/17.
 */
@Entity
@Data
@ToString(exclude = "year")
public class AssBoundary {
    @Id @GeneratedValue
    private long id;
    private double value;

    @ManyToOne (fetch = FetchType.LAZY)
    private Year year;

}

package ru.ifmo.neerc.volunteers.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.type.YesNoType;

import javax.persistence.*;

/**
 * Created by Matvey on 12/10/17.
 */
@Entity
@Data
@ToString(exclude = "year")
@NoArgsConstructor
public class AssBoundary {
    @Id @GeneratedValue
    private long id;
    private double value;

    @ManyToOne (fetch = FetchType.LAZY)
    private Year year;

    public AssBoundary(final double value, final Year year) {
        this.value = value;
        this.year = year;
    }

}

package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import ru.ifmo.neerc.volunteers.form.PositionForm;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;

/**
 * Created by Lapenok Akesej on 08.03.2017.
 */
@Entity
@Data
@RequiredArgsConstructor
public class PositionValue {

    @Id
    @GeneratedValue
    long id;

    @NonNull
    private String name;

    @NotNull
    @NonNull
    private boolean def;

    @NonNull
    private double value;

    @ManyToOne
    @NonNull
    private Year year;

    public PositionValue(PositionForm positionForm, Year year) {
        this(positionForm.getName(), false, positionForm.getValue(), year);
    }

    public PositionValue() {
    }

}

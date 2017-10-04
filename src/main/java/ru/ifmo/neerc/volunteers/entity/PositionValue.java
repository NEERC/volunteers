package ru.ifmo.neerc.volunteers.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
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
@JsonIgnoreProperties(value = {"year"})
@NoArgsConstructor
@AllArgsConstructor
@Builder
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
    @JsonIgnore
    private Year year;

    public PositionValue(PositionForm positionForm, Year year) {
        this(positionForm.getName(), false, positionForm.getValue(), year);
    }

}

package ru.ifmo.neerc.volunteers.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.*;
import ru.ifmo.neerc.volunteers.form.PositionForm;

import javax.persistence.*;
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
@Table(indexes = {@Index(columnList = "inForm")})
public class PositionValue {

    @Id
    @GeneratedValue
    long id;

    @NonNull
    private String name;

    @NonNull
    private String engName;

    @NonNull
    private String curName;

    @NotNull
    @NonNull
    private boolean def;

    @NonNull
    private double value;

    @ManyToOne
    @NonNull
    @JsonIgnore
    private Year year;

    @NonNull
    private long ord;

    @NonNull
    private boolean inForm;

    private boolean manager;

    public PositionValue(PositionForm positionForm, Year year) {
        this(positionForm.getName(), positionForm.getEngName(),
                positionForm.getCurName(), false, positionForm.getValue(), year,
                positionForm.getOrder(), positionForm.isForUser());
    }

    public void setFields(PositionForm positionForm) {
        setName(positionForm.getName());
        setEngName(positionForm.getEngName());
        setCurName(positionForm.getCurName());
        setValue(positionForm.getValue());
        setOrd(positionForm.getOrder());
        setInForm(positionForm.isForUser());
        setManager(positionForm.isManager());
    }

    public static PositionValue copyPosition(final PositionValue position, final Year year) {
        return new PositionValue(position.getName(), position.getEngName(),
                position.getCurName(), position.isDef(), position.getValue(), year,
                position.getOrd(), position.isInForm());
    }

}

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
@JsonIgnoreProperties(value = {"year"})
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

    @ManyToOne
    private Hall defaultHall;

    public PositionValue(long id, @NonNull String name, @NonNull String engName, @NonNull String curName, boolean def,
                         double value, @NonNull Year year, long ord, boolean inForm, boolean manager, Hall defaultHall) {
        this.id = id;
        this.name = name;
        this.engName = engName;
        this.curName = curName;
        this.def = def;
        this.value = value;
        this.year = year;
        this.ord = ord;
        this.inForm = inForm;
        this.manager = manager;
        this.defaultHall = defaultHall;
    }

    public PositionValue(@NonNull String name, @NonNull String engName, @NonNull String curName, boolean def,
                         double value, @NonNull Year year, long ord, boolean inForm, boolean manager,
                         Hall defaultHall) {
        this.name = name;
        this.engName = engName;
        this.curName = curName;
        this.def = def;
        this.value = value;
        this.year = year;
        this.ord = ord;
        this.inForm = inForm;
        this.manager = manager;
        this.defaultHall = defaultHall;
    }

    public PositionValue() {
    }

    public PositionValue(PositionForm positionForm, Year year, Hall defaultHall) {
        this(positionForm.getName(), positionForm.getEngName(),
                positionForm.getCurName(), false, positionForm.getValue(), year,
                positionForm.getOrder(), positionForm.isForUser(), positionForm.isManager(), defaultHall);
    }

    public void setFields(PositionForm positionForm, Hall defaultHall) {
        setName(positionForm.getName());
        setEngName(positionForm.getEngName());
        setCurName(positionForm.getCurName());
        setValue(positionForm.getValue());
        setOrd(positionForm.getOrder());
        setInForm(positionForm.isForUser());
        setManager(positionForm.isManager());
        setDefaultHall(defaultHall);
    }

    public static PositionValue copyPosition(final PositionValue position, final Year year) {
        return new PositionValue(position.getName(), position.getEngName(),
                position.getCurName(), position.isDef(), position.getValue(), year,
                position.getOrd(), position.isInForm(), position.isManager(), position.defaultHall);
    }

}

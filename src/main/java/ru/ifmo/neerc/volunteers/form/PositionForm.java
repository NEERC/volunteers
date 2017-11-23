package ru.ifmo.neerc.volunteers.form;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

/**
 * Created by Lapenok Akesej on 19.03.2017.
 */
@Data
public class PositionForm {

    @NotNull
    @NotEmpty(message = "надо указать имя")
    private String name;

    @NotNull
    @NotEmpty()
    private String curName;

    @Min(0)
    private double value;

    private Long order = 0L;

    private boolean forUser;
}

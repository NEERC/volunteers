package ru.ifmo.neerc.volunteers.form;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Created by Lapenok Akesej on 10.07.2017.
 */
@Data
public class HallForm {

    @NotEmpty
    String name;

    @NotEmpty
    String curName;

    @NotEmpty
    String description;
}

package ru.ifmo.neerc.volunteers.form;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.NotEmpty;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MedalForm {

    @NotEmpty
    private String name;

    @NotEmpty
    private String curName;

    private long value;

    private long stars;
}

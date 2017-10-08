package ru.ifmo.neerc.volunteers.form;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

/**
 * Created by Lapenok Akesej on 08.10.2017.
 */
@Data
public class MailForm {
    @NotEmpty
    String subject;

    @NotEmpty
    String body;
}

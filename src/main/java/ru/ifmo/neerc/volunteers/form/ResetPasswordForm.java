package ru.ifmo.neerc.volunteers.form;

import lombok.Data;
import org.hibernate.validator.constraints.Email;

/**
 * Created by Lapenok Akesej on 09.09.2017.
 */
@Data
public class ResetPasswordForm {

    @Email
    String email;
}

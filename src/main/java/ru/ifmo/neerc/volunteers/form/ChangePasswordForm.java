package ru.ifmo.neerc.volunteers.form;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.AssertTrue;

/**
 * Created by Lapenok Akesej on 09.09.2017.
 */
@Data
public class ChangePasswordForm {

    @NotEmpty
    String password;

    @NotEmpty
    String confirmPassword;

    transient boolean passwordEquals;

    @AssertTrue
    public boolean isPasswordEquals() {
        return password == null && confirmPassword == null ||
                password != null && confirmPassword != null && password.equals(confirmPassword);
    }
}

package ru.ifmo.neerc.volunteers.form;

import lombok.Data;
import lombok.ToString;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.AssertTrue;

/**
 * Created by Алексей on 21.02.2017.
 */
@Data
@ToString(exclude = {"password","confirmPassword","passwordEquals"})
public class UserForm {

    @NotEmpty
    private String firstName;

    @NotEmpty
    private String lastName;

    @NotEmpty
    private String firstNameCyr;

    @NotEmpty
    private String lastNameCyr;

    @NotEmpty
    private String password;

    @NotEmpty
    private String confirmPassword;

    @NotEmpty
    private String badgeName;

    @NotEmpty
    private String badgeNameCyr;

    @NotEmpty
    @Email
    private String email;

    transient private boolean passwordEquals;

    @AssertTrue(message = "Пароли не совпадают")
    public boolean isPasswordEquals() {
        return password == null && confirmPassword == null ||
                password != null && confirmPassword != null && password.equals(confirmPassword);
    }

}
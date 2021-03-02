package ru.ifmo.neerc.volunteers.form;

import lombok.Data;
import lombok.ToString;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Pattern;

/**
 * Created by Алексей on 21.02.2017.
 */
@Data
@ToString(exclude = {"password", "confirmPassword", "passwordEquals"})
public class UserForm {

    @NotEmpty
    @Pattern(regexp = "[a-zA-Z ,.'-]+")
    private String firstName;

    @NotEmpty
    @Pattern(regexp = "[a-zA-Z ,.'-]+")
    private String lastName;

    @NotEmpty
    @Pattern(regexp = "[а-яА-ЯёЁ ,.'-]+")
    private String firstNameCyr;

    @NotEmpty
    @Pattern(regexp = "[а-яА-ЯёЁ ,.'-]+")
    private String lastNameCyr;

    @NotEmpty
    private String password;

    @NotEmpty
    private String confirmPassword;

    //@NotEmpty
    private String badgeName;

    //@NotEmpty
    private String badgeNameCyr;

    @NotEmpty
    @Pattern(regexp = "\\+7[\\(]\\d{3}[\\)]\\d{3}[\\-]\\d{2}[\\-]\\d{2}")
    private String phone;

    @Pattern(regexp = "(\\d{6})?")
    private String itmoId;

    @NotEmpty
    @Email
    private String email;

    @NotEmpty
    private String covidStatus;

    transient private boolean emailExist = false;

    transient private boolean passwordEquals;

    @AssertTrue
    public boolean isPasswordEquals() {
        return password == null && confirmPassword == null ||
                password != null && confirmPassword != null && password.equals(confirmPassword);
    }

}

package ru.ifmo.neerc.volunteers.form;

import lombok.Data;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;
import ru.ifmo.neerc.volunteers.entity.PositionValue;
import ru.ifmo.neerc.volunteers.entity.User;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Pattern;
import java.util.Set;

/**
 * Created by Lapenok Akesej on 03.09.2017.
 */
@Data
public class UserYearForm {

    @NotEmpty
    private String firstName;

    @NotEmpty
    private String lastName;

    @NotEmpty
    private String firstNameCyr;

    @NotEmpty
    private String lastNameCyr;

    @NotEmpty
    @Pattern(regexp = "\\+7[\\(]\\d{3}[\\)]\\d{3}[\\-]\\d{2}[\\-]\\d{2}")
    private String phone;

    @NotEmpty
    @Email
    private String email;

    Set<PositionValue> positions;

    private String suggestions;

    @NotEmpty
    private String group;

    @AssertTrue
    private boolean isAgree;

    public UserYearForm(User user) {
        firstName = user.getFirstName();
        lastName = user.getLastName();
        firstNameCyr = user.getFirstNameCyr();
        lastNameCyr = user.getLastNameCyr();
        phone = user.getPhone();
        email = user.getEmail();
    }

    public UserYearForm() {

    }
}

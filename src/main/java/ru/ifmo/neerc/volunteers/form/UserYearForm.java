package ru.ifmo.neerc.volunteers.form;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import ru.ifmo.neerc.volunteers.entity.ApplicationForm;
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
    @Pattern(regexp = "\\+7[\\(]\\d{3}[\\)]\\d{3}[\\-]\\d{2}[\\-]\\d{2}")
    private String phone;

    private String email;

    @AssertTrue
    private boolean emailCorrect;

    Set<PositionValue> positions;

    private String suggestions;

    @NotEmpty
    private String group;

    @AssertTrue
    private boolean agree;

    @Pattern(regexp = "(\\d{6})?")
    private String itmoId;

    @NotEmpty
    private String covidStatus;

    public UserYearForm(User user) {
        firstName = user.getFirstName();
        lastName = user.getLastName();
        firstNameCyr = user.getFirstNameCyr();
        lastNameCyr = user.getLastNameCyr();
        phone = user.getPhone();
        email = user.getEmail();
        itmoId = user.getItmoId();
    }

    public UserYearForm() {

    }

    public UserYearForm(ApplicationForm applicationForm) {
        this(applicationForm.getUser());
        positions = applicationForm.getPositions();
        group = applicationForm.getGroup();
        suggestions = applicationForm.getSuggestions();
        covidStatus = applicationForm.getCovidStatus();
    }
}

package ru.ifmo.neerc.volunteers.form;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import ru.ifmo.neerc.volunteers.entity.User;

import javax.validation.constraints.Pattern;

@Data
public class UserProfileForm {

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

    private String badgeName;

    private String badgeNameCyr;

    @NotEmpty
    @Pattern(regexp = "\\+7[\\(]\\d{3}[\\)]\\d{3}[\\-]\\d{2}[\\-]\\d{2}")
    private String phone;

    @NotEmpty
    private String telegram;

    @Pattern(regexp = "(\\d{6})?")
    private String itmoId;

    public UserProfileForm() {
    }

    public UserProfileForm(User user) {
        firstName = user.getFirstName();
        lastName = user.getLastName();
        firstNameCyr = user.getFirstNameCyr();
        lastNameCyr = user.getLastNameCyr();
        badgeName = user.getBadgeName();
        badgeNameCyr = user.getBadgeNameCyr();
        phone = user.getPhone();
        telegram = user.getTelegram();
        itmoId = user.getItmoId();
    }
}

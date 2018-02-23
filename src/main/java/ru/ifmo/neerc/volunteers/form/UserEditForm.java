package ru.ifmo.neerc.volunteers.form;

import lombok.Data;
import org.hibernate.validator.constraints.NotEmpty;
import ru.ifmo.neerc.volunteers.entity.User;

import javax.validation.constraints.Pattern;

@Data
public class UserEditForm {
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
    @Pattern(regexp = "[a-zA-Z ,.'-]+")
    private String badgeName;

    @NotEmpty
    @Pattern(regexp = "[а-яА-ЯёЁ ,.'-]+")
    private String badgeNameCyr;

    @NotEmpty
    @Pattern(regexp = "\\+7[\\(]\\d{3}[\\)]\\d{3}[\\-]\\d{2}[\\-]\\d{2}")
    private String phone;

    private boolean administrator;

    private boolean chatLoginAllowed;

    @Pattern(regexp = "[a-zA-Z0-9-_]*")
    private String chatAlias;

    @Pattern(regexp = "(\\d{6})?")
    private String itmoId;

    public UserEditForm() {
    }

    public UserEditForm(User user) {
        firstName = user.getFirstName();
        lastName = user.getLastName();
        firstNameCyr = user.getFirstNameCyr();
        lastNameCyr = user.getLastNameCyr();
        badgeName = user.getBadgeName();
        badgeNameCyr = user.getBadgeNameCyr();
        phone = user.getPhone();

        administrator = user.getRole().getName().equals("ROLE_ADMIN");

        chatLoginAllowed = user.isChatLoginAllowed();
        chatAlias = user.getChatAlias();
    }
}

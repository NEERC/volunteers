package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import ru.ifmo.neerc.volunteers.form.UserForm;
import ru.ifmo.neerc.volunteers.form.UserYearForm;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

/**
 * Created by Алексей on 21.02.2017.
 */
@Entity
@Data
@EqualsAndHashCode(exclude = {"password", "role", "id", "applicationForms"})
@ToString(exclude = {"password", "role", "id", "applicationForms", "year"})
@Table(indexes = {@Index(columnList = "email", unique = true)})
public class User {

    /*@Autowired
    PasswordEncoder passwordEncoder;*/

    @Id
    @GeneratedValue
    private long id;

    private String firstName;

    private String lastName;

    private String firstNameCyr;

    private String lastNameCyr;

    private String password;

    private String badgeName;

    private String badgeNameCyr;

    private String email;

    private String phone;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    //@Column(firstName = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    private Year year;

    @OneToMany(mappedBy = "user")
    private Set<ApplicationForm> applicationForms;

    public User() {
    }

    public User(UserForm userForm) {
        firstName = userForm.getFirstName();
        lastName = userForm.getLastName();
        firstNameCyr = userForm.getFirstNameCyr();
        lastNameCyr = userForm.getLastNameCyr();
        password = userForm.getPassword();
        badgeName = userForm.getBadgeName();
        badgeNameCyr = userForm.getBadgeNameCyr();
        email = userForm.getEmail();
        phone = userForm.getPhone();
    }

    public Collection<Role> getAuth() {
        final List<Role> roles = new ArrayList<>(1);
        roles.add(getRole());
        return roles;
    }

    public boolean changeUserInformation(UserYearForm form) {
        boolean result = false;
        if (!firstName.equals(form.getFirstName())) {
            firstName = form.getFirstName();
            result = true;
        }
        if (!lastName.equals(form.getLastName())) {
            lastName = form.getLastName();
            result = true;
        }

        if (!firstNameCyr.equals(form.getFirstNameCyr())) {
            firstNameCyr = form.getFirstNameCyr();
            result = true;
        }
        if (!lastNameCyr.equals(form.getLastNameCyr())) {
            lastNameCyr = form.getLastNameCyr();
            result = true;
        }
        if (result) {
            badgeNameCyr = firstNameCyr + " " + lastNameCyr;
            badgeName = firstName + " " + lastName;
        }

        /*if (!email.equals(form.getEmail())) {
            email = form.getEmail();
            result = true;
        }*/
        if (!phone.equals(form.getPhone())) {
            phone = form.getPhone();
            result = true;
        }
        return result;
    }

}

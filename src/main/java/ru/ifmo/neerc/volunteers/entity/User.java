package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import lombok.ToString;
import ru.ifmo.neerc.volunteers.form.UserForm;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Алексей on 21.02.2017.
 */
@Entity
@Data
@ToString(exclude = {"password","role","id"})
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

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    //@Column(firstName = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    private Year year;

    public User(){}

    public User(UserForm userForm) {
        firstName=userForm.getFirstName();
        lastName=userForm.getLastName();
        firstNameCyr=userForm.getFirstNameCyr();
        lastNameCyr=userForm.getLastNameCyr();
        password=userForm.getPassword();
        badgeName=userForm.getBadgeName();
        badgeNameCyr=userForm.getBadgeNameCyr();
        email=userForm.getEmail();
    }

    public Collection<Role> getAuth() {
        final List<Role> roles = new ArrayList<>(1);
        roles.add(getRole());
        return roles;
    }

}

package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import lombok.ToString;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotEmpty;

import javax.persistence.*;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Created by Алексей on 21.02.2017.
 */
@Entity
@Data
@ToString(exclude = {"password","confirmPassword","role","passwordEquals","id"})
@Table(indexes = {@Index(columnList = "email", unique = true)})
public class User {

    @Id
    @GeneratedValue
    private long id;

    @NotEmpty(message = "надо написать имя")
    @Size(max = 50)
    private String firstName;

    @NotEmpty(message = "надо написать фамилию")
    @Size(max = 50)
    private String lastName;

    @NotEmpty
    private String firstNameCyr;

    @NotEmpty
    private String lastNameCyr;

    @NotEmpty(message = "надо придумать пароль")
    @Size(max = 250)
    private String password;

    @NotNull(message = "надо подтвердить пароль")
    @Size(max = 250)
    transient private String confirmPassword;

    @NotEmpty(message = "надо придумать badgeName firstName")
    @Size(max = 50)
    private String badgeName;

    @NotEmpty
    private String badgeNameCyr;

    @NotEmpty(message = "надо указать email")
    @Size(max = 250)
    @Email
    private String email;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    //@Column(firstName = "role_id", nullable = false)
    private Role role;

    transient private boolean passwordEquals;

    @AssertTrue(message = "Пароли не совпадают")
    public boolean isPasswordEquals() {
        return password == null && confirmPassword == null ||
                password != null && confirmPassword != null && password.equals(confirmPassword);
    }

    public Collection<Role> getAuth() {
        final List<Role> roles = new ArrayList<>(1);
        roles.add(getRole());
        return roles;
    }

}

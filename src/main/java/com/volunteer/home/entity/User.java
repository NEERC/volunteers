package com.volunteer.home.entity;

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
    private String name;

    @NotEmpty(message = "надо написать фамилию")
    @Size(max = 50)
    private String surname;

    @NotEmpty(message = "надо придумать пароль")
    @Size(max = 250)
    private String password;

    @NotNull(message = "надо подтвердить пароль")
    @Size(max = 250)
    transient private String confirmPassword;

    @NotEmpty(message = "надо придумать badge name")
    @Size(max = 50)
    private String badge;

    @NotEmpty(message = "надо указать email")
    @Size(max = 250)
    @Email
    private String email;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    //@Column(name = "role_id", nullable = false)
    private Role role;

    transient private boolean passwordEquals;
/*
    @ManyToMany(mappedBy = "user_roles")
    private Set<Role> roles;*/

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

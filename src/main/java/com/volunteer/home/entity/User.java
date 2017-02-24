package com.volunteer.home.entity;

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
/*@FieldMatch.List(value =
        {@FieldMatch(first = "password",second = "confirmPassword",message = "Пароли не совпадают")})*/
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
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

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE,CascadeType.PERSIST})
    //@Column(name = "role_id", nullable = false)
    private Role role;

    transient private boolean passwordEquals;
/*
    @ManyToMany(mappedBy = "user_roles")
    private Set<Role> roles;*/

    public User() {
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSurname() {
        return surname;
    }

    public void setSurname(String surname) {
        this.surname = surname;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    @AssertTrue(message = "Пароли не совпадают")
    public boolean isPasswordEquals() {
        return (password == null && confirmPassword == null) ||
                (password != null && confirmPassword != null && password.equals(confirmPassword));
    }

    public String getBadge() {
        return badge;
    }

    public void setBadge(String badge) {
        this.badge = badge;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Collection<Role> getAuth() {
        List<Role> roles=new ArrayList<>(1);
        roles.add(getRole());
        return roles;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", surname='" + surname + '\'' +
                ", password='" + password + '\'' +
                ", badge='" + badge + '\'' +
                '}';
    }
}

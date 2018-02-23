package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import ru.ifmo.neerc.volunteers.form.UserForm;
import ru.ifmo.neerc.volunteers.form.UserProfileForm;
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
@Table(indexes = {@Index(columnList = "email", unique = true), @Index(columnList = "year_id")})
@ToString(exclude = {"password", "role", "id", "applicationForms", "year"})
public class User implements UserDetails {

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

    private String oldEmail;

    private String phone;

    private String itmoId = "";

    private boolean confirmed;
    private boolean hq;

    @ManyToOne(fetch = FetchType.EAGER, cascade = {CascadeType.MERGE, CascadeType.PERSIST})
    //@Column(firstName = "role_id", nullable = false)
    private Role role;

    @ManyToOne(fetch = FetchType.LAZY)
    private Year year;

    @OneToMany(mappedBy = "user")
    private Set<ApplicationForm> applicationForms;

    private boolean chatLoginAllowed = true;

    private String chatAlias;

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
        confirmed = false;
        itmoId = userForm.getItmoId();
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
        if (phone == null || !phone.equals(form.getPhone())) {
            phone = form.getPhone();
            result = true;
        }

        if (itmoId == null || !itmoId.equals(form.getItmoId())) {
            itmoId = form.getItmoId();
            result = true;
        }
        return result;
    }

    public void updateProfile(UserProfileForm profile) {
        firstName = profile.getFirstName();
        lastName = profile.getLastName();
        firstNameCyr = profile.getFirstNameCyr();
        lastNameCyr = profile.getLastNameCyr();
        badgeName = profile.getBadgeName();
        badgeNameCyr = profile.getBadgeNameCyr();
        phone = profile.getPhone();
        itmoId = profile.getItmoId();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        final List<Role> roles = new ArrayList<>(1);
        roles.add(getRole());
        return roles;
    }

    @Override
    public String getUsername() {
        return getEmail();
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}

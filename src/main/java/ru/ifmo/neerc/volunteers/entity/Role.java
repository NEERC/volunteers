package ru.ifmo.neerc.volunteers.entity;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.springframework.security.core.GrantedAuthority;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

/**
 * Created by Алексей on 23.02.2017.
 */
@Entity
@Data
@EqualsAndHashCode(exclude = {"users"})
@ToString(exclude = {"users"})
public class Role implements Serializable, GrantedAuthority {

    @Id
    @GeneratedValue
    private long id;

    private String name;

    @OrderBy("badgeName ASC")
    @OneToMany(mappedBy = "role")
    Set<User> users;

    @Override
    public String getAuthority() {
        return getName();
    }
}

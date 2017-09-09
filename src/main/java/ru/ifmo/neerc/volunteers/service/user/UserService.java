package ru.ifmo.neerc.volunteers.service.user;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.core.Authentication;
import ru.ifmo.neerc.volunteers.entity.ResetPasswordToken;
import ru.ifmo.neerc.volunteers.entity.User;
import ru.ifmo.neerc.volunteers.entity.Year;
import ru.ifmo.neerc.volunteers.form.ChangePasswordForm;
import ru.ifmo.neerc.volunteers.form.ResetPasswordForm;
import ru.ifmo.neerc.volunteers.form.UserForm;

import java.util.Locale;
import java.util.Optional;

/**
 * Created by Lapenok Akesej on 03.09.2017.
 */
public interface UserService {

    User getUserByAuthentication(final Authentication authentication);

    void setUserYear(final User user, final Year year);

    Optional<ResetPasswordToken> resetPassword(final ResetPasswordForm form);

    void resetPassword(final ChangePasswordForm form);

    void registrateUser(final UserForm userForm);

    void changeUserPassword(final User user, final String password);

    Optional<String> validateResetPasswordToken(final long userId, final String token);

    SimpleMailMessage constructResetTokenEmail(final String contextPath, final Locale locale, final ResetPasswordToken token, final User user);
}

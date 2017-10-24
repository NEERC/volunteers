package ru.ifmo.neerc.volunteers.service.user;

import org.springframework.security.core.Authentication;
import ru.ifmo.neerc.volunteers.entity.ResetPasswordToken;
import ru.ifmo.neerc.volunteers.entity.User;
import ru.ifmo.neerc.volunteers.entity.Year;
import ru.ifmo.neerc.volunteers.form.ChangePasswordForm;
import ru.ifmo.neerc.volunteers.form.EmailForm;
import ru.ifmo.neerc.volunteers.form.UserForm;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Locale;
import java.util.Optional;

/**
 * Created by Lapenok Akesej on 03.09.2017.
 */
public interface UserService {

    User getUserByAuthentication(final Authentication authentication);

    void setUserYear(final User user, final Year year);

    Optional<ResetPasswordToken> resetPassword(final EmailForm form);

    void changePassword(final ChangePasswordForm form);

    User registrateUser(final UserForm userForm);

    void changeUserPassword(final User user, final String password);

    Optional<String> validateResetPasswordToken(final long userId, final String token, final String hash);

    MimeMessage constructResetTokenEmail(final String contextPath, final Locale locale, final ResetPasswordToken token) throws MessagingException;

    MimeMessage constructConfirmEmail(final User user, String contextPath, Locale locale) throws MessagingException;

    void confirmEmail(final User user, final String email);

    void changeEmail(final User user, final EmailForm emailForm, Authentication authentication);
}

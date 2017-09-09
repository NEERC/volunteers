package ru.ifmo.neerc.volunteers.service.user;

import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.ifmo.neerc.volunteers.config.SecurityConfig;
import ru.ifmo.neerc.volunteers.entity.ResetPasswordToken;
import ru.ifmo.neerc.volunteers.entity.Role;
import ru.ifmo.neerc.volunteers.entity.User;
import ru.ifmo.neerc.volunteers.entity.Year;
import ru.ifmo.neerc.volunteers.form.ChangePasswordForm;
import ru.ifmo.neerc.volunteers.form.ResetPasswordForm;
import ru.ifmo.neerc.volunteers.form.UserForm;
import ru.ifmo.neerc.volunteers.repository.ResetPasswordTokenRepository;
import ru.ifmo.neerc.volunteers.repository.RoleRepository;
import ru.ifmo.neerc.volunteers.repository.UserRepository;
import ru.ifmo.neerc.volunteers.service.Utils;
import ru.ifmo.neerc.volunteers.service.mail.EmailService;
import ru.ifmo.neerc.volunteers.service.security.SecurityService;

import java.util.*;

/**
 * Created by Lapenok Akesej on 03.09.2017.
 */
@Service
@AllArgsConstructor
public class UserServiceImpl implements UserService {

    final UserRepository userRepository;
    final ResetPasswordTokenRepository resetPasswordTokenRepository;
    final RoleRepository roleRepository;

    final PasswordEncoder passwordEncoder;

    final MailSender mailSender;
    final MessageSource messageSource;

    final SecurityService securityService;
    final EmailService emailService;
    final Utils utils;

    @Override
    public User getUserByAuthentication(final Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName());
    }

    @Override
    public void setUserYear(final User user, final Year year) {
        if (user.getYear() == null || user.getYear().getId() != year.getId()) {
            user.setYear(year);
            userRepository.save(user);
        }
    }

    @Override
    public Optional<ResetPasswordToken> resetPassword(final ResetPasswordForm form) {
        User user = userRepository.findByEmailIgnoreCase(form.getEmail());
        if (user == null) {
            return Optional.empty();
        }

        ResetPasswordToken token = new ResetPasswordToken(user);
        token.setExpiryDay(new Date());
        token.setToken(UUID.randomUUID().toString());
        token.setUsed(false);
        resetPasswordTokenRepository.save(token);
        return Optional.of(token);
    }

    public SimpleMailMessage constructResetTokenEmail(final String contextPath, final Locale locale, final ResetPasswordToken token, final User user) {
        String url = contextPath + "/changePassword/?id=" + user.getId() + "&token=" + token.getToken();
        String message = messageSource.getMessage("volunteers.email.resetPassword.email", new Object[]{url}, locale);
        return emailService.constructEmail(messageSource.getMessage("volunteers.email.resetPassword.subject", null, locale), message, user);
    }

    @Override
    public void resetPassword(final ChangePasswordForm form) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        changeUserPassword(user, form.getPassword());
    }

    @Override
    public void changeUserPassword(final User user, final String password) {
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    @Override
    public void registrateUser(final UserForm userForm) {
        User user = new User(userForm);
        Role role = roleRepository.findByName("ROLE_USER");//ROLE_USER
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        securityService.autologin(user.getEmail(), userForm.getPassword());
    }

    @Override
    public Optional<String> validateResetPasswordToken(final long userId, final String token) {
        ResetPasswordToken passwordToken = resetPasswordTokenRepository.findByToken(token);
        if (passwordToken == null || passwordToken.isUsed() || passwordToken.getUser().getId() != userId) {
            return Optional.of("InvalidToken");
        }
        Date now = new Date();
        if (now.getTime() - passwordToken.getExpiryDay().getTime() > ResetPasswordToken.EXPIRATION) {
            return Optional.of("Expired");
        }
        User user = passwordToken.getUser();
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null,
                Arrays.asList(new SimpleGrantedAuthority(SecurityConfig.PASSWORD_RESET_AUTHORITY)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        passwordToken.setUsed(true);
        resetPasswordTokenRepository.save(passwordToken);
        return Optional.empty();
    }
}

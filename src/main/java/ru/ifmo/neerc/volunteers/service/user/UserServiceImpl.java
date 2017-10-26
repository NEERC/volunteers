package ru.ifmo.neerc.volunteers.service.user;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.mail.MailSender;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.thymeleaf.context.Context;
import ru.ifmo.neerc.volunteers.config.SecurityConfig;
import ru.ifmo.neerc.volunteers.entity.*;
import ru.ifmo.neerc.volunteers.form.ChangePasswordForm;
import ru.ifmo.neerc.volunteers.form.EmailForm;
import ru.ifmo.neerc.volunteers.form.UserForm;
import ru.ifmo.neerc.volunteers.repository.ResetPasswordTokenRepository;
import ru.ifmo.neerc.volunteers.repository.RoleRepository;
import ru.ifmo.neerc.volunteers.repository.UserRepository;
import ru.ifmo.neerc.volunteers.service.Utils;
import ru.ifmo.neerc.volunteers.service.mail.EmailService;
import ru.ifmo.neerc.volunteers.service.security.SecurityService;

import javax.mail.MessagingException;
import javax.xml.bind.DatatypeConverter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created by Lapenok Akesej on 03.09.2017.
 */
@Service
@AllArgsConstructor
@EnableScheduling
@Slf4j
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
        if (authentication != null) {
            String name = authentication.getName();
            return userRepository.findByEmailIgnoreCase(name);
        }
        return null;
    }

    @Override
    public void setUserYear(final User user, final Year year) {
        if (user.getYear() == null || user.getYear().getId() != year.getId()) {
            user.setYear(year);
            userRepository.save(user);
        }
    }

    @Override
    public Optional<ResetPasswordToken> resetPassword(final EmailForm form) {
        User user = userRepository.findByEmailIgnoreCase(form.getEmail());
        if (user == null) {
            return Optional.empty();
        }
        ResetPasswordToken token = new ResetPasswordToken(user);
        token.setExpiryDay(new Date());
        token.setToken(UUID.randomUUID().toString());
        while (resetPasswordTokenRepository.findByToken(token.getToken()) != null) {
            token.setToken(UUID.randomUUID().toString());
        }
        resetPasswordTokenRepository.save(token);
        return Optional.of(token);
    }

    @Override
    public List<Mail> constructResetTokenEmail(final String contextPath, final Locale locale, final ResetPasswordToken token) throws MessagingException {
        String url = contextPath + "/changePassword/?id=" + token.getUser().getId() + "&token=" + token.getToken() + "&hash=" + getHash(token);
        String message = messageSource.getMessage("volunteers.email.resetPassword.email", new Object[]{url}, locale);
        Context context = new Context();
        context.setVariable("url", url);
        return emailService.constructEmail(messageSource.getMessage("volunteers.email.resetPassword.subject", null, locale), "mails/ResetPassword", context, token.getUser());
    }

    private String getHash(ResetPasswordToken token) {
        byte[] digest = DigestUtils.md5Digest((token.getToken() + " " + token.getUser().getPassword()).getBytes());
        return DatatypeConverter.printHexBinary(digest).toUpperCase();
    }

    @Override
    public void changePassword(final ChangePasswordForm form) {
        User user = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        changeUserPassword(user, form.getPassword());
    }

    @Override
    public void changeUserPassword(final User user, final String password) {
        user.setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
    }

    @Override
    public User registrateUser(final UserForm userForm) {
        User user = new User(userForm);
        Role role = roleRepository.findByName("ROLE_USER");//ROLE_USER
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        securityService.autologin(user.getEmail(), userForm.getPassword());
        return user;
    }

    @Override
    public Optional<String> validateResetPasswordToken(final long userId, final String token, final String hash) {
        ResetPasswordToken passwordToken = resetPasswordTokenRepository.findByToken(token);
        if (passwordToken == null || passwordToken.getUser().getId() != userId || !getHash(passwordToken).equals(hash)) {
            return Optional.of("InvalidToken");
        }
        Date now = new Date();
        if (now.getTime() - passwordToken.getExpiryDay().getTime() > ResetPasswordToken.EXPIRATION) {
            resetPasswordTokenRepository.delete(passwordToken);
            return Optional.of("Expired");
        }
        User user = passwordToken.getUser();
        Authentication authentication = new UsernamePasswordAuthenticationToken(user, null,
                Arrays.asList(new SimpleGrantedAuthority(SecurityConfig.PASSWORD_RESET_AUTHORITY)));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        resetPasswordTokenRepository.delete(passwordToken);
        return Optional.empty();
    }

    @Scheduled(fixedDelay = 24 * 60 * 60 * 1000)
    public void deleteExpiredTokens() {
        Set<ResetPasswordToken> tokens = resetPasswordTokenRepository.findAll();
        Date now = new Date();
        tokens = tokens.stream().filter(token -> now.getTime() - token.getExpiryDay().getTime() > ResetPasswordToken.EXPIRATION).collect(Collectors.toSet());
        resetPasswordTokenRepository.delete(tokens);
    }

    @Override
    public List<Mail> constructConfirmEmail(User user, String contextPath, Locale locale) throws MessagingException {
        String url = contextPath + "/confirm/?id=" + user.getId() + "&email=" + user.getEmail();
        String message = messageSource.getMessage("volunteers.email.confirm.email", new Object[]{url}, locale);
        Context context = new Context();
        context.setVariable("url", url);
        return emailService.constructEmail(messageSource.getMessage("volunteers.email.confirm.subject", null, locale), "mails/confirmMail", context, user);
    }

    @Override
    public void confirmEmail(User user, String email) {
        if (user.getEmail() == null) {
            log.error("User or email is null");
        }
        if (!user.getEmail().equalsIgnoreCase(email)) {
            return;
        }
        log.info("Marking user {} as confirmed", user.getEmail());
        user.setConfirmed(true);
        userRepository.save(user);
    }

    @Override
    public void changeEmail(User user, EmailForm emailForm, Authentication authentication) {
        if (userRepository.findByEmailIgnoreCase(emailForm.getEmail()) != null) {
            return;
        }
        if (user.isConfirmed())
            user.setOldEmail(user.getEmail());
        user.setEmail(emailForm.getEmail());
        user.setConfirmed(false);
        userRepository.save(user);
        User userDetails = (User) authentication.getPrincipal();
        userDetails.setEmail(emailForm.getEmail());
    }
}

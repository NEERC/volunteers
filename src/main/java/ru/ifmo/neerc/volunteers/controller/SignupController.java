package ru.ifmo.neerc.volunteers.controller;

import lombok.AllArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.thymeleaf.spring.support.Layout;
import ru.ifmo.neerc.volunteers.entity.User;
import ru.ifmo.neerc.volunteers.form.UserForm;
import ru.ifmo.neerc.volunteers.repository.UserRepository;
import ru.ifmo.neerc.volunteers.service.Utils;
import ru.ifmo.neerc.volunteers.service.mail.EmailService;
import ru.ifmo.neerc.volunteers.service.user.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Locale;

/**
 * Created by Алексей on 16.02.2017.
 */
@Controller
@Layout("empty")
@EnableTransactionManagement
@AllArgsConstructor
public class SignupController {

    private static final Logger logger = LoggerFactory.getLogger(SignupController.class);

    final UserRepository userRepository;

    final UserService userService;

    final EmailService emailService;

    final Utils utils;

    private final Locale locale = Locale.getDefault();

    @GetMapping("/signup")
    public String signup(@ModelAttribute("user") UserForm user) {
        return "signup";
    }

    @PostMapping("/signup")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String processSignup(@Valid @ModelAttribute("user") UserForm userForm, BindingResult result, HttpServletRequest request) {
        if (userRepository.findByEmailIgnoreCase(userForm.getEmail()) != null) {
            result.rejectValue("emailExist", "exist.user.email", "");
        }
        userForm.setBadgeName(userForm.getFirstName() + " " + userForm.getLastName());
        userForm.setBadgeNameCyr(userForm.getFirstNameCyr() + " " + userForm.getLastNameCyr());
        if (result.hasErrors()) {
            return "signup";
        }
        User user = userService.registrateUser(userForm);
        emailService.sendSimpleMessage(userService.constructConfirmEmail(user, utils.getAppUrl(request), locale));
        return "redirect:/";
    }
}

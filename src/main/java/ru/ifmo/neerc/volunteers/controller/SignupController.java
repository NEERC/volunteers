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
import ru.ifmo.neerc.volunteers.form.UserForm;
import ru.ifmo.neerc.volunteers.repository.UserRepository;
import ru.ifmo.neerc.volunteers.service.user.UserService;

import javax.validation.Valid;

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

    @GetMapping("/signup")
    public String signup(@ModelAttribute("user") UserForm user) {
        return "signup";
    }

    @PostMapping("/signup")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String processSignup(@Valid @ModelAttribute("user") UserForm userForm, BindingResult result) {
        if (userRepository.findByEmailIgnoreCase(userForm.getEmail()) != null) {
            result.rejectValue("emailExist", "exist.user.email", "");
        }
        userForm.setBadgeName(userForm.getFirstName() + " " + userForm.getLastName());
        userForm.setBadgeNameCyr(userForm.getFirstNameCyr() + " " + userForm.getLastNameCyr());
        if (result.hasErrors()) {
            return "signup";
        }
        userService.registrateUser(userForm);
        return "redirect:/";
    }
}

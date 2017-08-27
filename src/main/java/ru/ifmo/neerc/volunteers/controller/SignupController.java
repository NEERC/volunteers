package ru.ifmo.neerc.volunteers.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.thymeleaf.spring.support.Layout;
import ru.ifmo.neerc.volunteers.entity.Role;
import ru.ifmo.neerc.volunteers.entity.User;
import ru.ifmo.neerc.volunteers.form.UserForm;
import ru.ifmo.neerc.volunteers.repository.RoleRepository;
import ru.ifmo.neerc.volunteers.repository.UserRepository;
import ru.ifmo.neerc.volunteers.service.SecurityService;

import javax.validation.Valid;

/**
 * Created by Алексей on 16.02.2017.
 */
@Controller
@Layout("empty")
@EnableTransactionManagement
public class SignupController {

    private static final Logger logger = LoggerFactory.getLogger(SignupController.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    SecurityService securityService;

    @Autowired
    PasswordEncoder passwordEncoder;

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
        if (result.hasErrors()) {
            return "signup";
        }
        User user = new User(userForm);
        logger.debug(String.format("User created %s", user.toString()));
        Role role = roleRepository.findByName("ROLE_USER");//ROLE_USER
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
        securityService.autologin(user.getEmail(), userForm.getPassword());
        return "redirect:/years";
    }
}

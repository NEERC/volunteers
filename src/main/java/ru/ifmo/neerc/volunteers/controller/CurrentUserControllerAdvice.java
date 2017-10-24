package ru.ifmo.neerc.volunteers.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import ru.ifmo.neerc.volunteers.entity.User;
import ru.ifmo.neerc.volunteers.repository.UserRepository;

@ControllerAdvice
public class CurrentUserControllerAdvice {

    @Autowired
    UserRepository userRepository;

    @ModelAttribute("currentUser")
    public User populateCurrentUser(Authentication authentication) {
        if (authentication == null)
            return null;
        return userRepository.findByEmailIgnoreCase(authentication.getName());
    }
}

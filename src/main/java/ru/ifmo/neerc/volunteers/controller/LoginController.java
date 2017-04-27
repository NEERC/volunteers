package ru.ifmo.neerc.volunteers.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.thymeleaf.spring.support.Layout;

/**
 * Created by Алексей on 17.02.2017.
 */
@Controller
public class LoginController {

    @RequestMapping("/login")
    @Layout("empty")
    public String login(@RequestParam(value = "error", required = false) final String error,
                        @RequestParam(value = "logout", required = false) final String logout,
                        final Model model) {

        if (error != null) {
            model.addAttribute("error", "");
        }
        if (logout != null) {
            model.addAttribute("message", "");
        }
        return "login";
    }
}

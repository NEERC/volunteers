package ru.ifmo.neerc.volunteers.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by Алексей on 17.02.2017.
 */
@Controller
public class LoginController {

    @RequestMapping("/login")
    public String login(@RequestParam(value = "error", required = false) String error,
                        @RequestParam(value = "signup", required = false) String signup,
                        @RequestParam(value = "logout", required = false) String logout,
                        Model model) {

        if (error != null) {
            model.addAttribute("error", "Invalid UserId or Password");

        }
        if (signup != null) {
            model.addAttribute("message", "singup was successful");
        }
        if (logout != null) {
            model.addAttribute("message", "logout was successful");
        }
        return "login";
    }
}

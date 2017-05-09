package ru.ifmo.neerc.volunteers.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Set;

/**
 * Created by Алексей on 15.02.2017.
 */
@Controller
public class SimpleController {
    @RequestMapping("/")
    public String home(Authentication authentication) {
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        if (roles.contains("ROLE_ADMIN")) {
            return "redirect:/admin";
        }
        if (roles.contains("ROLE_USER")) {
            return "redirect:/years";
        }
        return "redirect:/login";
    }
}

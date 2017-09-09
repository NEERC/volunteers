package ru.ifmo.neerc.volunteers.controller;

import lombok.AllArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.spring.support.Layout;
import ru.ifmo.neerc.volunteers.entity.ApplicationForm;
import ru.ifmo.neerc.volunteers.entity.User;
import ru.ifmo.neerc.volunteers.entity.Year;
import ru.ifmo.neerc.volunteers.form.UserYearForm;
import ru.ifmo.neerc.volunteers.repository.YearRepository;
import ru.ifmo.neerc.volunteers.service.Utils;
import ru.ifmo.neerc.volunteers.service.user.UserService;
import ru.ifmo.neerc.volunteers.service.year.YearService;

import javax.validation.Valid;
import java.util.Optional;
import java.util.Set;

/**
 * Created by Lapenok Akesej on 26.02.2017.
 */
@Controller
@RequestMapping("/")
@Layout("publicUser")
@AllArgsConstructor
public class UserController {

    final YearRepository yearRepository;

    final UserService userService;
    final YearService yearService;
    final Utils utils;

    @RequestMapping
    public String home(final Authentication authentication) {
        final User user = userService.getUserByAuthentication(authentication);
        Set<String> roles = AuthorityUtils.authorityListToSet(authentication.getAuthorities());
        StringBuilder redirectString = new StringBuilder("redirect:/");
        boolean isAdmin = false;
        if (roles.contains("ROLE_ADMIN")) {
            redirectString.append("admin/");
            isAdmin = true;
        }
        Optional<Year> currentYear = Optional.ofNullable(user.getYear());
        if (!currentYear.isPresent()) {
            currentYear = yearService.getLastYear();
        }
        if (currentYear.isPresent()) {
            return redirectString.append("year/").append(currentYear.get().getId()).toString();
        }
        if (isAdmin)
            return redirectString.toString();
        return "home";
    }

    @GetMapping("/year/{id}")
    public String years(@PathVariable final long id, final Model model, final Authentication authentication) {
        User user = userService.getUserByAuthentication(authentication);
        Year year = yearRepository.findOne(id);
        userService.setUserYear(user, year);
        utils.setModelForUser(model, year);
        if (!model.containsAttribute("applicationForm")) {
            ApplicationForm form = yearService.getApplicationForm(user, year);
            model.addAttribute("applicationForm", new UserYearForm(form));
            model.addAttribute("isSaved", form.getId() != 0);
        }
        return "year";
    }

    @PostMapping("/year/{id}/signup")
    public String signupForYear(@PathVariable final long id, @Valid @ModelAttribute("applicationForm") final UserYearForm applicationForm, final BindingResult result, final Model model, final Authentication authentication, final RedirectAttributes attributes) {
        if (result.hasErrors()) {
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.applicationForm", result);
            attributes.addFlashAttribute("applicationForm", applicationForm);
            return "redirect:/year/" + id;
        }
        yearService.regUser(userService.getUserByAuthentication(authentication), applicationForm, yearRepository.findOne(id));
        return "redirect:/year/" + id;
    }
}

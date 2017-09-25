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
import ru.ifmo.neerc.volunteers.entity.Day;
import ru.ifmo.neerc.volunteers.entity.User;
import ru.ifmo.neerc.volunteers.entity.Year;
import ru.ifmo.neerc.volunteers.form.EmailForm;
import ru.ifmo.neerc.volunteers.form.UserYearForm;
import ru.ifmo.neerc.volunteers.repository.DayRepository;
import ru.ifmo.neerc.volunteers.repository.UserRepository;
import ru.ifmo.neerc.volunteers.repository.YearRepository;
import ru.ifmo.neerc.volunteers.service.Utils;
import ru.ifmo.neerc.volunteers.service.day.DayService;
import ru.ifmo.neerc.volunteers.service.mail.EmailService;
import ru.ifmo.neerc.volunteers.service.user.UserService;
import ru.ifmo.neerc.volunteers.service.year.YearService;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.Locale;
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
    final UserRepository userRepository;
    final DayRepository dayRepository;

    final UserService userService;
    final YearService yearService;
    final EmailService emailService;
    final DayService dayService;
    final Utils utils;

    private final Locale locale = Locale.getDefault();

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
        utils.setModelForUser(model, user);
        if (!model.containsAttribute("applicationForm")) {
            ApplicationForm form = yearService.getApplicationForm(user, year);
            model.addAttribute("applicationForm", new UserYearForm(form));
            model.addAttribute("isSaved", form.getId() != 0);
        }
        model.addAttribute("isConfirmed", user.isConfirmed());
        return "year";
    }

    @PostMapping("/year/{id}/signup")
    public String signupForYear(@PathVariable final long id, @Valid @ModelAttribute("applicationForm") final UserYearForm applicationForm, final BindingResult result, final Authentication authentication, final RedirectAttributes attributes) {
        User user = userService.getUserByAuthentication(authentication);
        applicationForm.setEmail(user.getEmail());
        if (result.hasErrors()) {
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.applicationForm", result);
            attributes.addFlashAttribute("applicationForm", applicationForm);
            return "redirect:/year/" + id;
        }
        yearService.regUser(userService.getUserByAuthentication(authentication), applicationForm, yearRepository.findOne(id));
        return "redirect:/year/" + id;
    }

    @GetMapping("/position")
    public String positions(final Model model, final Authentication authentication) {
        utils.setModelForUser(model, userService.getUserByAuthentication(authentication));
        model.addAttribute("title", "Positions");
        return "position";
    }

    @GetMapping("/hall")
    public String hall(final Model model, final Authentication authentication) {
        utils.setModelForUser(model, userService.getUserByAuthentication(authentication));
        model.addAttribute("title", "Halls");
        return "hall";
    }

    @GetMapping("/user/confirm")
    public String sendConfirmEmail(final Authentication authentication, final HttpServletRequest request) {
        emailService.sendSimpleMessage(
                userService.constructConfirmEmail(
                        userService.getUserByAuthentication(authentication),
                        utils.getAppUrl(request), locale
                )
        );
        return "redirect:/";
    }

    @PostMapping("/user/email")
    public String changeEmail(@Valid @ModelAttribute("emailForm") final EmailForm emailForm, final BindingResult result, final Authentication authentication, final RedirectAttributes attributes, final HttpServletRequest request) {
        User user = userService.getUserByAuthentication(authentication);
        if (userRepository.findByEmailIgnoreCase(emailForm.getEmail()) != null &&
                !user.getEmail().equals(emailForm.getEmail())) {
            result.rejectValue("emailExist", "exist.emailForm.email", "");
        }
        if (result.hasErrors()) {
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.emailForm", result);
            attributes.addFlashAttribute("emailForm", emailForm);
        } else {
            userService.changeEmail(user, emailForm, authentication);
            emailService.sendSimpleMessage(userService.constructConfirmEmail(user, utils.getAppUrl(request), locale));
        }
        return "redirect:/";
    }

    @GetMapping("/day/{id}")
    public String getDay(@PathVariable("id") final long id, final Model model, final Authentication authentication) {
        User user = userService.getUserByAuthentication(authentication);
        final Day day = dayRepository.findOne(id);

        utils.setModelForUser(model, userService.getUserByAuthentication(authentication));
        model.addAttribute("hallUser", dayService.getHallUser(day, locale));
        model.addAttribute("day", day);
        model.addAttribute("halls", user.getYear().getHalls());
        model.addAttribute("title", day.getName());
        return "showEvent";
    }
}

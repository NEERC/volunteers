package ru.ifmo.neerc.volunteers.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.spring.support.Layout;
import ru.ifmo.neerc.volunteers.entity.ResetPasswordToken;
import ru.ifmo.neerc.volunteers.entity.User;
import ru.ifmo.neerc.volunteers.form.ChangePasswordForm;
import ru.ifmo.neerc.volunteers.form.EmailForm;
import ru.ifmo.neerc.volunteers.modal.JsonResponse;
import ru.ifmo.neerc.volunteers.modal.Status;
import ru.ifmo.neerc.volunteers.repository.UserRepository;
import ru.ifmo.neerc.volunteers.service.Utils;
import ru.ifmo.neerc.volunteers.service.mail.EmailService;
import ru.ifmo.neerc.volunteers.service.user.UserService;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.Locale;
import java.util.Optional;

/**
 * Created by Алексей on 17.02.2017.
 */
@Controller
@AllArgsConstructor
@Layout("empty")
@Slf4j
public class LoginController {

    final UserService userService;
    final EmailService emailService;
    final Utils utils;

    final UserRepository userRepository;

    @RequestMapping("/login")
    public String login(@RequestParam(value = "error", required = false) final String error,
                        @RequestParam(value = "reset", required = false) final String reset,
                        final Model model) {

        model.addAttribute("error", error);
        model.addAttribute("resetPasswordForm", new EmailForm());

        /*if (logout != null) {
            model.addAttribute("message", "");
        }*/
        if (reset != null) {
            model.addAttribute("reset", "");
        }
        return "login";
    }

    @PostMapping("/reset-password/")
    public @ResponseBody
    JsonResponse<String> resetPassword(@Valid @ModelAttribute("resetPasswordForm") final EmailForm form, final BindingResult result, final HttpServletRequest request, final Locale locale) throws MessagingException {
        JsonResponse<String> response = new JsonResponse<>();
        if (result.hasErrors()) {
            response.setStatus(Status.FAIL);
            response.setResult(result.getAllErrors().toString());
            return response;
        }
        Optional<ResetPasswordToken> token = userService.resetPassword(form);
        if (token.isPresent()) {
            response.setStatus(Status.OK);
            emailService.sendSimpleMessage(
                    userService.constructResetTokenEmail(utils.getAppUrl(request), locale, token.get()));
        } else {
            response.setStatus(Status.FAIL);
            response.setResult("");
        }
        return response;
    }

    @GetMapping("/changePassword")
    public String showChangePasswordPage(@RequestParam("id") final long id, @RequestParam("token") final String token, @RequestParam("hash") final String hash) {
        Optional<String> result = userService.validateResetPasswordToken(id, token, hash);
        if (result.isPresent()) {
            return "redirect:/login";
        }
        return "redirect:/updatePassword";
    }

    @GetMapping("/updatePassword")
    public String updatePassword(final Model model) {
        if (!model.containsAttribute("form"))
            model.addAttribute("form", new ChangePasswordForm());
        return "changePassword";
    }

    @PostMapping("/updatePassword")
    public String updatePassword(@Valid @ModelAttribute("form") final ChangePasswordForm form, final BindingResult result, final Model model) {
        if (result.hasErrors()) {
            return updatePassword(model);
        }
        userService.changePassword(form);
        SecurityContextHolder.getContext().setAuthentication(null);
        return "redirect:/login?reset";
    }

    @GetMapping(value = "/confirm")
    public String confirmEmail(@RequestParam("id") final long id, @RequestParam("email") final String email, HttpServletResponse resp) throws IOException {
        User user = userRepository.findOne(id);
        if (user == null) {
            log.warn("User with id={} is not found", id);
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        log.info("Confirming user id={}, email={}", id, email);
        userService.confirmEmail(user, email);
        return "redirect:/";
    }

}

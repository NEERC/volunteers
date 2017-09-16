package ru.ifmo.neerc.volunteers.controller;

import lombok.AllArgsConstructor;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.thymeleaf.spring.support.Layout;
import ru.ifmo.neerc.volunteers.service.Utils;
import ru.ifmo.neerc.volunteers.service.user.UserService;
import ru.ifmo.neerc.volunteers.service.year.YearService;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Алексей on 16.02.2017.
 */
@Controller
@Layout("publicUser")
@AllArgsConstructor
public class MyErrorController implements ErrorController, AccessDeniedHandler {

    private static final String ERROR_PATH = "/error";

    private final ErrorAttributes errorAttributes;
    private final Utils utils;
    private final UserService userService;
    private final YearService yearService;

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }

    @RequestMapping(ERROR_PATH)
    String error(final HttpServletRequest request, final Model model, final Authentication authentication) {
        Map<String, Object> errorMap = errorAttributes.getErrorAttributes(new ServletRequestAttributes(request), true);
        if (!errorMap.containsKey("trace"))
            errorMap.put("trace", "");
        model.addAttribute("errors", errorMap);
        utils.setModelForUser(model, yearService.getYear(userService.getUserByAuthentication(authentication)));
        return "error";
    }

    @RequestMapping("/403")
    String error403(final HttpServletRequest request, final Model model, final Authentication authentication) {
        HashMap<String, Object> errorMap = new HashMap<>();
        errorMap.put("status", "403");
        errorMap.put("message", "AccessDenied");
        model.addAttribute("errors", errorMap);
        utils.setModelForUser(model, userService.getUserByAuthentication(authentication).getYear());
        return "error";
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.sendRedirect("/403");
    }
}

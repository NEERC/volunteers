package com.volunteer.home.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.context.request.ServletRequestAttributes;

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
public class MyErrorController implements ErrorController, AccessDeniedHandler {

    private static final String ERROR_PATH = "/error";

    @Autowired
    private ErrorAttributes errorAttributes;

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }

    @RequestMapping(ERROR_PATH)
    String error(HttpServletRequest request, Model model) {
        Map<String, Object> errorMap = errorAttributes.getErrorAttributes(new ServletRequestAttributes(request), true);
        if (!errorMap.containsKey("trace"))
            errorMap.put("trace", "");
        model.addAttribute("errors", errorMap);

        return "error";
    }

    @RequestMapping("/403")
    String error403(HttpServletRequest request, Model model) {
        HashMap<String, Object> errorMap = new HashMap<>();
        errorMap.put("status", "403");
        errorMap.put("message", "AccessDenied");
        model.addAttribute("errors", errorMap);
        return "error";
    }

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        response.sendRedirect("/403");
    }
}

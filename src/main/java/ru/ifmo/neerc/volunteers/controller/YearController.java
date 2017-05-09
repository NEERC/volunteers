package ru.ifmo.neerc.volunteers.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.thymeleaf.spring.support.Layout;

import ru.ifmo.neerc.volunteers.repository.YearRepository;

@Controller
@Layout("public")
public class YearController {

    @Autowired
    YearRepository yearRepository;

    @GetMapping("/years")
    public String years(Model model) {
        model.addAttribute("years", yearRepository.findAllByOrderByIdDesc());
        return "years";
    }
}

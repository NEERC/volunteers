package ru.ifmo.neerc.volunteers.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.spring.support.Layout;
import ru.ifmo.neerc.volunteers.entity.ApplicationForm;
import ru.ifmo.neerc.volunteers.entity.Year;
import ru.ifmo.neerc.volunteers.repository.PositionValueRepository;
import ru.ifmo.neerc.volunteers.repository.YearRepository;

import javax.validation.Valid;

@Controller
@Layout("public")
public class YearController {

    @Autowired
    PositionValueRepository positionValueRepository;

    @Autowired
    YearRepository yearRepository;

    @GetMapping("/years")
    public String years(Model model) {
        model.addAttribute("years", yearRepository.findAllByOrderByIdDesc());
        return "years";
    }

    @GetMapping("/years/{id}/signup")
    public String signupForYear(@PathVariable long id, Model model) {
        Year year = yearRepository.findOne(id);
        model.addAttribute("year", year);
        model.addAttribute("positions", year.getPositionValues());

        if (!model.containsAttribute("applicationForm")) {
            model.addAttribute("applicationForm", new ApplicationForm());
        }

        return "yearSignup";
    }

    @PostMapping("/years/{id}/signup")
    public String signupForYear(@PathVariable long id, @Valid @ModelAttribute("applicationForm") ApplicationForm applicationForm, BindingResult result, RedirectAttributes attributes) {
        attributes.addFlashAttribute("org.springframework.validation.BindingResult.applicationForm", result);
        attributes.addFlashAttribute("applicationForm", applicationForm);
        return "redirect:/years/" + id + "/signup";
    }
}

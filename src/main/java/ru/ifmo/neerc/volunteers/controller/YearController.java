package ru.ifmo.neerc.volunteers.controller;

import lombok.AllArgsConstructor;
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
import ru.ifmo.neerc.volunteers.repository.ApplicationFormRepository;
import ru.ifmo.neerc.volunteers.repository.PositionValueRepository;
import ru.ifmo.neerc.volunteers.repository.YearRepository;

import javax.validation.Valid;

@Controller
@Layout("public")
@AllArgsConstructor
public class YearController {

    final PositionValueRepository positionValueRepository;
    final YearRepository yearRepository;
    final ApplicationFormRepository applicationFormRepository;

    @GetMapping("/years")
    public String years(Model model) {
        model.addAttribute("years", yearRepository.findAllByOrderByIdDesc());
        return "years";
    }

    @PostMapping("/years/{id}/signup")
    public String signupForYear(@PathVariable long id, @Valid @ModelAttribute("applicationForm") ApplicationForm applicationForm, BindingResult result, RedirectAttributes attributes) {
        if (result.hasErrors()) {
            return "yearSignup";
        }
        applicationForm.setYear(yearRepository.findOne(id));
        applicationFormRepository.save(applicationForm);
        return "redirect:/years/";
    }
}

package com.volunteer.home.controller;

import com.volunteer.home.entity.Event;
import com.volunteer.home.entity.Year;
import com.volunteer.home.repository.MyEventRepository;
import com.volunteer.home.repository.MyYearRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Set;

/**
 * Created by Lapenok Akesej on 25.02.2017.
 */
@RequestMapping("/admin")
@Controller
public class AdminController {

    @Autowired
    MyYearRepository yearRepository;

    @Autowired
    MyEventRepository eventRepository;

    @RequestMapping(method = RequestMethod.GET)
    public String admin(Year year, Model model) {
        Set<Year> years = yearRepository.findByCurrentOrderByIdAsc(true);
        model.addAttribute("current_years", years);

        return "admin";
    }

    @RequestMapping(value = "/year/add", method = RequestMethod.POST)
    public String addYear(@Valid @ModelAttribute("year") Year year, BindingResult result) {
        if (result.hasErrors()) {
            return "admin";
        }

        year.setCurrent(true);
        year.setOpen(true);
        yearRepository.save(year);
        return "redirect:/admin/year?id=" + year.getId();
    }

    @RequestMapping(value = "/year")
    public String showYear(@RequestParam(value = "id") long id, Event event, Model model) {
        Year year = yearRepository.findOne(id);
        model.addAttribute("year",year);
        event.setYear(year);
        model.addAttribute("events",year.getEvents());
        return "year";
    }

    @RequestMapping(value = "/event/add")
    public String addEvent(@Valid @ModelAttribute("event") Event event, BindingResult result) {
        Year year=event.getYear();
        if(result.hasErrors()){
            return "/admin/year?id="+year.getId();
        }
        eventRepository.save(event);
        year.getEvents().add(event);
        return "redirect:/admin/year?id="+event.getYear().getId();
    }

    @RequestMapping(value = "/event")
    public String showEvent(@RequestParam(value = "id") long id, Model model) {
        Event event=eventRepository.findOne(id);
        model.addAttribute("event",event);
        return "event";
    }
}

package ru.ifmo.neerc.volunteers.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import ru.ifmo.neerc.volunteers.entity.*;
import ru.ifmo.neerc.volunteers.repository.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;

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

    @Autowired
    MyPositionRepository positionRepository;

    @Autowired
    MyHallRepository hallRepository;

    @Autowired
    UserEventRepository userEventRepository;

    @RequestMapping(method = RequestMethod.GET)
    public String admin(Model model) {
        Set<Year> years = yearRepository.findByCurrentOrderByIdAsc(true);
        model.addAttribute("current_years", years);
        Set<Position> positions = positionRepository.findAll();
        model.addAttribute("positions", positions);
        model.addAttribute("halls", hallRepository.findAll());
        if (!model.containsAttribute("position")) {
            model.addAttribute("position", new Position());
        }
        if (!model.containsAttribute("year")) {
            model.addAttribute("year", new Year());
        }
        if (!model.containsAttribute("hall")) {
            model.addAttribute("hall", new Hall());
        }
        return "admin";
    }

    @RequestMapping(value = "/position/add", method = RequestMethod.POST)
    public String addPosition(@Valid @ModelAttribute("position") final Position position, final BindingResult result, RedirectAttributes attributes) {
        if (result.hasErrors()) {
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.position", result);
            attributes.addFlashAttribute("position", position);
            return "redirect:/admin";
        }
        positionRepository.save(position);
        return "redirect:/admin";
    }

    @RequestMapping(value = "/hall/add", method = RequestMethod.POST)
    public String addHall(@Valid @ModelAttribute("hall") Hall hall, BindingResult result, RedirectAttributes attributes) {
        if (result.hasErrors()) {
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.hall", result);
            attributes.addFlashAttribute("hall", hall);
            return "redirect:/admin";
        }
        hallRepository.save(hall);
        return "redirect:/admin";
    }

    @RequestMapping(value = "/year/add", method = RequestMethod.POST)
    public String addYear(@Valid @ModelAttribute("year") Year year, BindingResult result, RedirectAttributes attributes) {
        if (result.hasErrors()) {
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.year", result);
            attributes.addFlashAttribute("year", year);
            return "redirect:/admin";
        }
        year.setCurrent(true);
        year.setOpen(true);
        yearRepository.save(year);
        return "redirect:/admin/year?id=" + year.getId();
    }

    @RequestMapping(value = "/year")
    public String showYear(@RequestParam(value = "id") long id, Model model) {
        Year year = yearRepository.findOne(id);
        model.addAttribute("year", year);
        model.addAttribute("events", year.getEvents());
        Set<ApplicationForm> users = year.getUsers();
        model.addAttribute("users", users);
        if (!model.containsAttribute("event")) {
            Event event = new Event();
            event.setYear(year);
            model.addAttribute("event", event);
        }
        return "year";
    }

    @RequestMapping(value = "/event/add")
    public String addEvent(@Valid @ModelAttribute("event") Event event, BindingResult result, RedirectAttributes attributes) {
        Year year = event.getYear();
        if (result.hasErrors()) {
            if (year != null) {
                attributes.addFlashAttribute("org.springframework.validation.BindingResult.event", result);
                attributes.addFlashAttribute("event", event);
                return "redirect:/admin/year?id=" + year.getId();
            } else
                return "redirect:/admin";
        }
        eventRepository.save(event);
        year.getEvents().add(event);
        yearRepository.save(year);
        Set<ApplicationForm> users = year.getUsers();
        event.setUsers(new HashSet<>());
        Position position = positionRepository.findOne(1l);//default position
        Hall hall = hallRepository.findOne(1l);//default hall
        for (ApplicationForm user : users) {
            UserEvent userEvent = new UserEvent();
            userEvent.setEvent(event);
            userEvent.setHall(hall);
            userEvent.setPosition(position);
            userEvent.setUserYear(user);
            userEventRepository.save(userEvent);//save new user
            event.getUsers().add(userEvent);
        }
        eventRepository.save(event);
        return "redirect:/admin/event?id=" + event.getId();
    }

    @RequestMapping(value = "event")
    public String showEvent(@RequestParam(value = "id") long id, Model model) {
        Event event = eventRepository.findOne(id);
        Set<UserEvent> users = event.getUsers();
        HashMap<Hall, List<UserEvent>> hallUser = new HashMap<>();
        for (UserEvent user : users) {
            if (!hallUser.containsKey(user.getHall()))
                hallUser.put(user.getHall(), new ArrayList<>());
            hallUser.get(user.getHall()).add(user);
        }
        Set<Hall> halls = hallRepository.findAll();
        for (Hall hall : halls) {
            if (!hallUser.containsKey(hall))
                hallUser.put(hall, new ArrayList<>());
        }
        model.addAttribute("hallUser", hallUser);
        model.addAttribute("event", event);
        model.addAttribute("halls", hallRepository.findAll());
        return "showEvent";
    }

    @RequestMapping(value = "/event/edit")
    public String editEvent(@RequestParam(value = "id") long id, Model model) {
        Event event = eventRepository.findOne(id);
        model.addAttribute("event", event);
        model.addAttribute("users", event.getUsers());
        model.addAttribute("positions", positionRepository.findAll());
        model.addAttribute("halls", hallRepository.findAll());
        Set<Event> events = event.getYear().getEvents();
        events.remove(event);

        model.addAttribute("events", events);
        return "event";
    }

    @RequestMapping(value = "/event/save")
    public String save(HttpServletRequest request) {
        Event event = eventRepository.findOne(Long.parseLong(request.getParameter("event")));
        Set<UserEvent> users = event.getUsers();
        for (UserEvent user : users) {
            user.setPosition(positionRepository.findOne(
                    Long.parseLong(request.getParameter("p" + user.getId()))));
            user.setHall(hallRepository.findOne(
                    Long.parseLong(request.getParameter("h" + user.getId()))
            ));
            userEventRepository.save(user);
        }
        return "redirect:/admin/event?id=" + event.getId();
    }

    @RequestMapping(value = "/event/copy")
    public String copy(HttpServletRequest request) {
        Event event = eventRepository.findOne(Long.parseLong(request.getParameter("event")));
        Event baseEvent = eventRepository.findOne(Long.parseLong(request.getParameter("baseEvent")));
        Map<Long, UserEvent> userEventBase = new HashMap<>();
        for (UserEvent userEvent : baseEvent.getUsers()) {
            userEventBase.put(userEvent.getUserYear().getId(), userEvent);
        }
        Set<UserEvent> users = event.getUsers();
        for (UserEvent user : users) {
            Long form = user.getUserYear().getId();
            user.setHall(userEventBase.get(form).getHall());
            user.setPosition(userEventBase.get(form).getPosition());
            userEventRepository.save(user);
        }
        return "redirect:/admin/event/?id=" + event.getId();
    }
}

package ru.ifmo.neerc.volunteers.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.spring.support.Layout;
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
@Layout("publicAdmin")
public class AdminController {

    @Autowired
    YearRepository yearRepository;

    @Autowired
    EventRepository eventRepository;

    @Autowired
    PositionRepository positionRepository;

    @Autowired
    HallRepository hallRepository;

    @Autowired
    UserEventRepository userEventRepository;

    @Autowired
    UserRepository userRepository;

    @RequestMapping(method = RequestMethod.GET)
    public String admin(Model model, Authentication authentication) {
        User user = getUser(authentication);
        if (user.getYear() != null) {
            return "redirect:/admin/year?id=" + user.getYear().getId();
        }
        Iterable<Year> years = yearRepository.findAll();
        model.addAttribute("current_years", years);
        setModel(model,new Year("Year"));
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
    public String addPosition(@Valid @ModelAttribute("newPosition") final Position position, final BindingResult result, RedirectAttributes attributes, Authentication authentication) {
        User user=getUser(authentication);
        Year year=user.getYear();
        if (result.hasErrors()) {
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.position", result);
            attributes.addFlashAttribute("newPosition", position);
        }
        else
            positionRepository.save(position);
        return "redirect:/admin/year?id="+year.getId();
    }

    @RequestMapping(value = "/hall/add", method = RequestMethod.POST)
    public String addHall(@Valid @ModelAttribute("newHall") Hall hall, BindingResult result, RedirectAttributes attributes, Authentication authentication) {
        User user=getUser(authentication);
        Year year=user.getYear();
        if (result.hasErrors()) {
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.hall", result);
            attributes.addFlashAttribute("newHall", hall);
        }
        else {
            hall.setYear(year);
            hallRepository.save(hall);
            year.getHalls().add(hall);
            yearRepository.save(year);
        }
        return "redirect:/admin/year?id="+year.getId();
    }

    @RequestMapping(value = "/year/add", method = RequestMethod.POST)
    public String addYear(@Valid @ModelAttribute("newYear") Year year, BindingResult result, RedirectAttributes attributes, Authentication authentication) {
        if (result.hasErrors()) {
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.year", result);
            attributes.addFlashAttribute("newYear", year);
            Year year1=getUser(authentication).getYear();
            return "redirect:/admin/year?id="+year1.getId();
        }
        yearRepository.save(year);
        return "redirect:/admin/year?id=" + year.getId();
    }

    @RequestMapping(value = "/year")
    public String showYear(@RequestParam(value = "id") long id, Model model, Authentication authentication) {
        User user = getUser(authentication);

        Year year = yearRepository.findOne(id);
        if (user.getYear() == null || user.getYear().getId() != id) {
            user.setYear(year);
            userRepository.save(user);
        }
        setModel(model, year);
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
    public String addEvent(@Valid @ModelAttribute("event") Event event, BindingResult result, RedirectAttributes attributes, Authentication authentication) {
        User user=getUser(authentication);
        Year year = event.getYear();
        if(year==null) {
            year=user.getYear();
            event.setYear(year);
        }
        if (result.hasErrors()) {
            if (year != null) {
                attributes.addFlashAttribute("org.springframework.validation.BindingResult.event", result);
                attributes.addFlashAttribute("newEvent", event);
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
        for (ApplicationForm applicationForm: users) {
            UserEvent userEvent = new UserEvent();
            userEvent.setEvent(event);
            userEvent.setHall(hall);
            userEvent.setPosition(position);
            userEvent.setUserYear(applicationForm);
            userEventRepository.save(userEvent);//save new user
            event.getUsers().add(userEvent);
        }
        eventRepository.save(event);
        return "redirect:/admin/event?id=" + event.getId();
    }

    @RequestMapping(value = "event")
    public String showEvent(@RequestParam(value = "id") long id, Model model, Authentication authentication) {
        Year year=getUser(authentication).getYear();
        Event event = eventRepository.findOne(id);
        setModel(model,year);
        Set<UserEvent> users = event.getUsers();
        HashMap<Hall, List<UserEvent>> hallUser = new HashMap<>();
        for (UserEvent user : users) {
            if (!hallUser.containsKey(user.getHall()))
                hallUser.put(user.getHall(), new ArrayList<>());
            hallUser.get(user.getHall()).add(user);
        }
        Set<Hall> halls = year.getHalls();
        for (Hall hall : halls) {
            if (!hallUser.containsKey(hall))
                hallUser.put(hall, new ArrayList<>());
        }
        model.addAttribute("hallUser", hallUser);
        model.addAttribute("event", event);
        model.addAttribute("halls", halls);
        return "showEvent";
    }

    @RequestMapping(value = "/event/edit")
    public String editEvent(@RequestParam(value = "id") long id, Model model, Authentication authentication) {
        Year year=getUser(authentication).getYear();
        Event event = eventRepository.findOne(id);
        setModel(model,year);
        model.addAttribute("event", event);
        model.addAttribute("users", event.getUsers());
        model.addAttribute("positions", positionRepository.findAll());
        model.addAttribute("halls", year.getHalls());
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

    private User getUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName());
    }

    private void setModel(Model model, Year year) {
        model.addAttribute("year",year);
        if(!model.containsAttribute("newYear"))
            model.addAttribute("newYear",new Year());
        model.addAttribute("years", yearRepository.findAll());
        model.addAttribute("events", year.getEvents());
        if(!model.containsAttribute("newEvent")) {
            Event newEvent = new Event();
            newEvent.setYear(year);
            model.addAttribute("newEvent", newEvent);
        }
        model.addAttribute("positions", positionRepository.findAll());
        if(!model.containsAttribute("newPosition"))
            model.addAttribute("newPosition",new Position());
        model.addAttribute("halls", year.getHalls());
        if(!model.containsAttribute("newHall")) {
            Hall newHall = new Hall();
            newHall.setYear(year);
            model.addAttribute("newHall", newHall);
        }
    }
}

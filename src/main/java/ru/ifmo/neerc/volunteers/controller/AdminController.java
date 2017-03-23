package ru.ifmo.neerc.volunteers.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
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
import ru.ifmo.neerc.volunteers.form.PositionForm;
import ru.ifmo.neerc.volunteers.repository.*;

import javax.jws.soap.SOAPBinding;
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

    @Autowired
    PositionValueRepository positionValueRepository;

    @Autowired
    RoleRepository roleRepository;

    @RequestMapping(method = RequestMethod.GET)
    public String admin(Model model, Authentication authentication) {
        User user = getUser(authentication);
        if (user.getYear() != null) {
            return "redirect:/admin/year?id=" + user.getYear().getId();
        }
        List<Year> years = yearRepository.findAll();
        if (years.size() != 0) {
            return "redirect:/admin/year?id=" + years.get(years.size() - 1).getId();
        }
        setModel(model, null);
        /*if (!model.containsAttribute("position")) {
            model.addAttribute("position", new Position());
        }
        if (!model.containsAttribute("year")) {
            model.addAttribute("year", new Year());
        }
        if (!model.containsAttribute("hall")) {
            model.addAttribute("hall", new Hall());
        }*/
        return "admin";
    }

    @RequestMapping(value = "/position", method = RequestMethod.GET)
    public String positions(Model model, Authentication authentication) {
        setModel(model, getUser(authentication).getYear());
        model.addAttribute("title","Positions");
        return "position";
    }

    @RequestMapping(value = "/position/add", method = RequestMethod.POST)
    public String addPosition(@Valid @ModelAttribute("newPosition") final PositionForm positionForm, final BindingResult result, RedirectAttributes attributes, Authentication authentication) {
        User user = getUser(authentication);
        Year year = user.getYear();
        if (result.hasErrors()) {
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.position", result);
            attributes.addFlashAttribute("newPosition", positionForm);
        } else {
            Position position = new Position(positionForm);
            positionRepository.save(position);
            PositionValue positionValue = new PositionValue(position, year, positionForm.getValue());
            positionValueRepository.save(positionValue);
            year.getPositionValues().add(positionValue);
            yearRepository.save(year);
        }
        return "redirect:/admin/position";
    }

    @RequestMapping(value = "/position/values", method = RequestMethod.POST)
    public String setPositionValues(HttpServletRequest request, Authentication authentication) {
        Year year = getUser(authentication).getYear();
        Set<PositionValue> positionValues = year.getPositionValues();
        for (PositionValue positionValue : positionValues) {
            int value = Integer.parseInt(request.getParameter("v" + positionValue.getId()));
            if (positionValue.getValue() != value) {
                positionValue.setValue(value);
                positionValueRepository.save(positionValue);
            }
        }
        return "redirect:/admin/year?id=" + year.getId();
    }

    @RequestMapping(value = "/position/delete")
    public String deletePosition(@RequestParam("id") long id, Authentication authentication) {
        if (id != 1) {
            Year year = getUser(authentication).getYear();
            PositionValue positionValue = positionValueRepository.findOne(id);
            year.getPositionValues().remove(positionValue);
            yearRepository.save(year);
            positionValueRepository.delete(id);
        }
        return "redirect:/admin/position";
    }

    @RequestMapping(value = "/hall")
    public String hall(Model model, Authentication authentication) {
        setModel(model,getUser(authentication).getYear());
        model.addAttribute("title","Halls");
        return "hall";
    }

    @RequestMapping(value = "/hall/add", method = RequestMethod.POST)
    public String addHall(@Valid @ModelAttribute("newHall") Hall hall, BindingResult result, RedirectAttributes attributes, Authentication authentication) {
        User user = getUser(authentication);
        Year year = user.getYear();
        if (result.hasErrors()) {
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.hall", result);
            attributes.addFlashAttribute("newHall", hall);
        } else {
            hall.setYear(year);
            hallRepository.save(hall);
            year.getHalls().add(hall);
            yearRepository.save(year);
        }
        return "redirect:/admin/hall";
    }

    @RequestMapping(value = "/year/add", method = RequestMethod.POST)
    public String addYear(@Valid @ModelAttribute("newYear") Year year, BindingResult result, RedirectAttributes attributes, Authentication authentication) {
        Year yearOld = getUser(authentication).getYear();
        if (result.hasErrors()) {
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.year", result);
            attributes.addFlashAttribute("newYear", year);
            if(yearOld!=null)
                return "redirect:/admin/year?id=" + yearOld.getId();
            else
                return "redirect:/admin";
        }
        Set<Position> positions = positionRepository.findAll();
        yearRepository.save(year);
        if(yearOld!=null) {
            Set<PositionValue> positionValuesOld = yearOld.getPositionValues();
            Map<Long, Integer> positionValueMap = new HashMap<>();
            for (PositionValue positionValue : positionValuesOld) {
                positionValueMap.put(positionValue.getPosition().getId(), positionValue.getValue());
            }

            Set<PositionValue> positionValues = new HashSet<>();
            for (Position position : positions) {
                if (positionValueMap.containsKey(position.getId())) {
                    PositionValue positionValue = new PositionValue(position, year, positionValueMap.get(position.getId()));
                    positionValues.add(positionValue);
                }
            }

            positionValueRepository.save(positionValues);
            year.setPositionValues(positionValues);
        }
        else {
            year.setPositionValues(new HashSet<>());
            for(Position position:positions) {
                year.getPositionValues().add(new PositionValue(position,year,0));
            }
            positionValueRepository.save(year.getPositionValues());
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
        model.addAttribute("title",year.getName());
        return "year";
    }

    @RequestMapping(value = "/event/add")
    public String addEvent(@Valid @ModelAttribute("event") Event event, BindingResult result, RedirectAttributes attributes, Authentication authentication) {
        User user = getUser(authentication);
        Year year = event.getYear();
        if (year == null) {
            year = user.getYear();
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
        for (ApplicationForm applicationForm : users) {
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
        Year year = getUser(authentication).getYear();
        Event event = eventRepository.findOne(id);
        setModel(model, year);
        Set<UserEvent> users = event.getUsers();
        HashMap<Hall, List<UserEvent>> hallUser = new HashMap<>();
        for (UserEvent user : users) {
            if (!hallUser.containsKey(user.getHall())) {
                hallUser.put(user.getHall(), new ArrayList<>());
            }
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
        model.addAttribute("title",event.getName());
        return "showEvent";
    }

    @RequestMapping(value = "/event/edit")
    public String editEvent(@RequestParam(value = "id") long id, Model model, Authentication authentication) {
        Year year = getUser(authentication).getYear();
        Event event = eventRepository.findOne(id);
        setModel(model, year);
        model.addAttribute("event", event);
        model.addAttribute("users", event.getUsers());
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
            boolean flage = false;
            long newIdPosition = Long.parseLong(request.getParameter("p" + user.getId()));
            long newIdHall = Long.parseLong(request.getParameter("h" + user.getId()));
            if (user.getPosition().getId() != newIdPosition) {
                user.setPosition(positionValueRepository.findOne(newIdPosition).getPosition());
                flage = true;
            }
            if (user.getHall().getId() != newIdHall) {
                user.setHall(hallRepository.findOne(newIdHall));
                flage = true;
            }
            if (flage)
                userEventRepository.save(user);
        }
        return "redirect:/admin/event?id=" + event.getId();
    }

    @RequestMapping(value = "/event/copy")
    public String copy(HttpServletRequest request) {
        Event event = eventRepository.findOne(Long.parseLong(request.getParameter("event")));
        if(Long.parseLong(request.getParameter("baseEvent"))!=-1) {
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
        }
        return "redirect:/admin/event/?id=" + event.getId();
    }

    @RequestMapping(value = "/add")
    public String addAdmin(HttpServletRequest request) {
        Long id=Long.parseLong(request.getParameter("newAdmin"));
        Role roleUser=roleRepository.findOne(2l);
        Role roleAdmin=roleRepository.findOne(1l);
        User user=userRepository.findOne(id);
        roleUser.getUsers().remove(user);
        roleAdmin.getUsers().add(user);
        user.setRole(roleAdmin);
        userRepository.save(user);
        roleRepository.save(roleUser);
        roleRepository.save(roleAdmin);
        return "redirect:/admin";
    }

    private User getUser(Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName());
    }

    private void setModel(Model model, Year year) {
        model.addAttribute("year", year);
        if (!model.containsAttribute("newYear"))
            model.addAttribute("newYear", new Year());
        model.addAttribute("years", yearRepository.findAll());
        if(year!=null) {
            model.addAttribute("events", year.getEvents());
            model.addAttribute("positions", year.getPositionValues());
            model.addAttribute("halls", year.getHalls());
        }
        else {
            model.addAttribute("events", Collections.EMPTY_LIST);
            model.addAttribute("positions", Collections.EMPTY_LIST);
            model.addAttribute("halls", Collections.EMPTY_LIST);
        }
        if (!model.containsAttribute("newEvent")) {
            Event newEvent = new Event();
            newEvent.setYear(year);
            model.addAttribute("newEvent", newEvent);
        }
        if (!model.containsAttribute("newPosition"))
            model.addAttribute("newPosition", new PositionForm());
        if (!model.containsAttribute("newHall")) {
            Hall newHall = new Hall();
            newHall.setYear(year);
            model.addAttribute("newHall", newHall);
        }
        Role roleAdmin=roleRepository.findOne(1l);
        Role roleUser=roleRepository.findOne(2l);
        model.addAttribute("roleAdmin",roleAdmin.getUsers());
        model.addAttribute("roleUsers",roleUser.getUsers());
    }
}

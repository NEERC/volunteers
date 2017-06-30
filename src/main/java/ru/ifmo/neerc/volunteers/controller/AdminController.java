package ru.ifmo.neerc.volunteers.controller;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.context.MessageSource;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.spring.support.Layout;

import lombok.AllArgsConstructor;
import ru.ifmo.neerc.volunteers.entity.ApplicationForm;
import ru.ifmo.neerc.volunteers.entity.Attendance;
import ru.ifmo.neerc.volunteers.entity.Event;
import ru.ifmo.neerc.volunteers.entity.Hall;
import ru.ifmo.neerc.volunteers.entity.Medal;
import ru.ifmo.neerc.volunteers.entity.Position;
import ru.ifmo.neerc.volunteers.entity.PositionValue;
import ru.ifmo.neerc.volunteers.entity.Role;
import ru.ifmo.neerc.volunteers.entity.User;
import ru.ifmo.neerc.volunteers.entity.UserEvent;
import ru.ifmo.neerc.volunteers.entity.UserEventAssessment;
import ru.ifmo.neerc.volunteers.entity.Year;
import ru.ifmo.neerc.volunteers.form.PositionForm;
import ru.ifmo.neerc.volunteers.repository.ApplicationFormRepository;
import ru.ifmo.neerc.volunteers.repository.EventRepository;
import ru.ifmo.neerc.volunteers.repository.HallRepository;
import ru.ifmo.neerc.volunteers.repository.MedalRepository;
import ru.ifmo.neerc.volunteers.repository.PositionRepository;
import ru.ifmo.neerc.volunteers.repository.PositionValueRepository;
import ru.ifmo.neerc.volunteers.repository.RoleRepository;
import ru.ifmo.neerc.volunteers.repository.UserEventAssessmentRepository;
import ru.ifmo.neerc.volunteers.repository.UserEventRepository;
import ru.ifmo.neerc.volunteers.repository.UserRepository;
import ru.ifmo.neerc.volunteers.repository.YearRepository;

/**
 * Created by Lapenok Akesej on 25.02.2017.
 */
@RequestMapping("/admin")
@Controller
@Layout("publicAdmin")
@EnableTransactionManagement
@AllArgsConstructor
public class AdminController {

    YearRepository yearRepository;
    EventRepository eventRepository;
    PositionRepository positionRepository;
    HallRepository hallRepository;
    UserEventRepository userEventRepository;
    UserRepository userRepository;
    PositionValueRepository positionValueRepository;
    RoleRepository roleRepository;
    UserEventAssessmentRepository userEventAssessmentRepository;
    MedalRepository medalRepository;
    ApplicationFormRepository applicationFormRepository;
    MessageSource messageSource;

    @GetMapping
    public String admin(final Model model, final Authentication authentication) {
        final User user = getUser(authentication);
        if (user.getYear() != null) {
            return "redirect:/admin/year?id=" + user.getYear().getId();
        }
        final List<Year> years = yearRepository.findAll();
        if (years.size() != 0) {
            return "redirect:/admin/year?id=" + years.get(years.size() - 1).getId();
        }
        setModel(model, null);
        return "admin";
    }

    @GetMapping("/position")
    public String positions(final Model model, final Authentication authentication) {
        setModel(model, getUser(authentication).getYear());
        model.addAttribute("title", "Positions");
        return "position";
    }

    @RequestMapping(value = "/position/add", method = RequestMethod.POST)
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String addPosition(@Valid @ModelAttribute("newPosition") final PositionForm positionForm, final BindingResult result, final RedirectAttributes attributes, final Authentication authentication) {
        final User user = getUser(authentication);
        final Year year = user.getYear();
        if (result.hasErrors()) {
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.newPosition", result);
            attributes.addFlashAttribute("newPosition", positionForm);
        } else {
            final Position position = new Position(positionForm);
            positionRepository.save(position);
            final PositionValue positionValue = new PositionValue(position, year, positionForm.getValue());
            positionValueRepository.save(positionValue);
        }
        return "redirect:/admin/position";
    }

    @RequestMapping(value = "/position/values", method = RequestMethod.POST)
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String setPositionValues(final HttpServletRequest request, final Authentication authentication) {
        final Year year = getUser(authentication).getYear();
        final Set<PositionValue> positionValues = year.getPositionValues();
        for (final PositionValue positionValue : positionValues) {
            final Double value = Double.parseDouble(request.getParameter("v" + positionValue.getId()));
            if (positionValue.getValue() != value) {
                positionValue.setValue(value);
                positionValueRepository.save(positionValue);
            }
        }
        return "redirect:/admin/position";
    }

    @RequestMapping(value = "/position/delete")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String deletePosition(@RequestParam("id") final long id, final RedirectAttributes attributes, final Locale locale) {
        final Position position = positionValueRepository.findOne(id).getPosition();
        if (position.getId() != 1) {
            try {
                positionValueRepository.delete(id);
            } catch (final Exception e) {
                attributes.addFlashAttribute("message", messageSource.getMessage("volunteers.position.error.delete", new Object[]{position.getName()}, "Error to delete position", locale));
            }
        }
        return "redirect:/admin/position";
    }

    @RequestMapping(value = "/hall/delete")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String deleteHall(@RequestParam("id") final long id, final RedirectAttributes attributes, final Locale locale) {
        try {
            if (id != 1L) {
                hallRepository.delete(id);
            }
        } catch (final Exception e) {
            final Hall hall = hallRepository.findOne(id);
            attributes.addFlashAttribute("message", messageSource.getMessage("volunteers.hall.error.delete", new Object[]{hall.getName()}, "Error to delete hall", locale));
        }
        return "redirect:/admin/hall";
    }

    @RequestMapping(value = "/hall")
    public String hall(final Model model, final Authentication authentication) {
        setModel(model, getUser(authentication).getYear());
        model.addAttribute("title", "Halls");
        return "hall";
    }

    @RequestMapping(value = "/hall/add", method = RequestMethod.POST)
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String addHall(@Valid @ModelAttribute("newHall") final Hall hall, final BindingResult result, final RedirectAttributes attributes, final Authentication authentication) {
        final User user = getUser(authentication);
        final Year year = user.getYear();
        if (result.hasErrors()) {
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.newHall", result);
            attributes.addFlashAttribute("newHall", hall);
        } else {
            hall.setYear(year);
            hallRepository.save(hall);
        }
        return "redirect:/admin/hall";
    }

    @RequestMapping(value = "/year/add", method = RequestMethod.POST)
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String addYear(@Valid @ModelAttribute("newYear") final Year year, final BindingResult result, final RedirectAttributes attributes, final Authentication authentication) {
        final Year yearOld = getUser(authentication).getYear();
        if (result.hasErrors()) {
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.newYear", result);
            attributes.addFlashAttribute("newYear", year);
            if (yearOld != null) {
                return "redirect:/admin/year?id=" + yearOld.getId();
            } else {
                return "redirect:/admin";
            }
        }
        final Set<Position> positions = positionRepository.findAll();
        year.setOpenForRegistration(true);
        yearRepository.save(year);
        if (yearOld != null) {
            final Set<PositionValue> positionValuesOld = yearOld.getPositionValues();
            final Map<Long, Double> positionValueMap = new HashMap<>();
            for (final PositionValue positionValue : positionValuesOld) {
                positionValueMap.put(positionValue.getPosition().getId(), positionValue.getValue());
            }

            final Set<PositionValue> positionValues = new HashSet<>();
            for (final Position position : positions) {
                if (positionValueMap.containsKey(position.getId())) {
                    final PositionValue positionValue = new PositionValue(position, year, positionValueMap.get(position.getId()));
                    positionValues.add(positionValue);
                }
            }
            positionValueRepository.save(positionValues);
        } else {
            final Set<PositionValue> positionValues = new HashSet<>();
            for (final Position position : positions) {
                positionValues.add(new PositionValue(position, year, 0));
            }
            positionValueRepository.save(positionValues);
        }
        return "redirect:/admin/year?id=" + year.getId();
    }

    @RequestMapping(value = "/year/close")
    public String closeYear(final Authentication authentication) {
        final Year year = getUser(authentication).getYear();
        year.setOpenForRegistration(false);
        yearRepository.save(year);
        return "redirect:/admin/year?id=" + year.getId();
    }

    @RequestMapping(value = "/year")
    public String showYear(@RequestParam(value = "id") final long id, final Model model, final Authentication authentication) {
        final User user = getUser(authentication);

        final Year year = yearRepository.findOne(id);
        if (user.getYear() == null || user.getYear().getId() != id) {
            user.setYear(year);
            userRepository.save(user);
        }
        setModel(model, year);
        final Set<ApplicationForm> users = year.getUsers();
        model.addAttribute("users", users);
        /*if (!model.containsAttribute("event")) {
            Event event = new Event();
            event.setYear(year);
            model.addAttribute("event", event);
        }*/
        model.addAttribute("title", year.getName());
        return "year";
    }

    @RequestMapping(value = "/event/add")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String addEvent(@Valid @ModelAttribute("newEvent") final Event event, final BindingResult result, final RedirectAttributes attributes, final Authentication authentication) throws Exception {
        final User user = getUser(authentication);
        Year year = event.getYear();
        if (year == null) {
            year = user.getYear();
            event.setYear(year);
        }
        if (result.hasErrors()) {
            if (year != null) {
                attributes.addFlashAttribute("org.springframework.validation.BindingResult.newEvent", result);
                attributes.addFlashAttribute("newEvent", event);
                return "redirect:/admin/year?id=" + year.getId();
            } else {
                return "redirect:/admin";
            }
        }
        eventRepository.save(event);
        final Set<ApplicationForm> users = year.getUsers();
        event.setUsers(new HashSet<>());
        final PositionValue positionValue = findOrCreateDefaultPosition(year);
        final Hall hall = hallRepository.findOne(1L);//default hall
        final List<UserEvent> userEvents = new ArrayList<>();
        final PositionValue finalPositionValue = positionValue;
        users.forEach(applicationForm -> {
            final UserEvent userEvent = new UserEvent();
            userEvent.setEvent(event);
            userEvent.setHall(hall);
            userEvent.setPosition(finalPositionValue);
            userEvent.setUserYear(applicationForm);
            userEvent.setAttendance(Attendance.YES);
            userEvents.add(userEvent);
        });
        userEventRepository.save(userEvents);
        return "redirect:/admin/event?id=" + event.getId();
    }

    private PositionValue findOrCreateDefaultPosition(final Year year) {
        PositionValue positionValue = null;
        for (final PositionValue positionValue1 : year.getPositionValues()) {
            if (positionValue1.getPosition().isDef()) {
                positionValue = positionValue1;
            }
        }
        if (positionValue == null) {
            positionValue = new PositionValue(positionRepository.findOne(1L), year, 0);
            positionValueRepository.save(positionValue);
        }
        return positionValue;
    }

    @RequestMapping(value = "event")
    public String event(@RequestParam(value = "id") final long id, final Model model, final Authentication authentication) {
        final Year year = getUser(authentication).getYear();
        final Event currentEvent = eventRepository.findOne(id);
        setModel(model, year);
        final Set<ApplicationForm> yearUsers = new HashSet<>(year.getUsers());
        yearUsers.removeAll(currentEvent.getUsers().stream().map(UserEvent::getUserYear).collect(Collectors.toSet()));
        final Hall reserve = hallRepository.findOne(1L);

        final PositionValue defaultPosition = findOrCreateDefaultPosition(year);

        userEventRepository.save(yearUsers.stream().map(af -> {
            final UserEvent ue = new UserEvent();
            ue.setUserYear(af);
            ue.setHall(reserve);
            ue.setPosition(defaultPosition);
            ue.setAttendance(Attendance.YES );
            currentEvent.addUser(ue);
            return ue;
        }).collect(Collectors.toList()));

        final Event event = eventRepository.save(currentEvent);


        final HashMap<Hall, List<UserEvent>> hallUser = new HashMap<>(
                event.getUsers().stream().collect(Collectors.groupingBy(ue -> ue.getHall())));

        final Set<Hall> halls = year.getHalls();
        hallUser.putAll(halls.stream()
                .filter(h -> !hallUser.containsKey(h))
                .collect(Collectors.toMap(Function.identity(), hall -> new ArrayList<>())));

        model.addAttribute("hallUser", hallUser);
        model.addAttribute("event", event);
        model.addAttribute("halls", halls);
        model.addAttribute("title", event.getName());
        return "showEvent";
    }

    @RequestMapping(value = "/event/edit")
    public String editEvent(@RequestParam(value = "id") final long id, final Model model, final Authentication authentication) {
        final Year year = getUser(authentication).getYear();
        final Event event = eventRepository.findOne(id);
        setModel(model, year);
        model.addAttribute("event", event);
        model.addAttribute("users", event.getUsers());
        return "event";
    }

    @RequestMapping(value = "/event/save")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String save(final HttpServletRequest request) {
        final Event event = eventRepository.findOne(Long.parseLong(request.getParameter("event")));
        final Set<UserEvent> users = event.getUsers();
        final Set<UserEvent> forSave = new HashSet<>();
        for (final UserEvent user : users) {
            boolean flage = false;
            final long newIdPosition = Long.parseLong(request.getParameter("p" + user.getId()));
            long newIdHall = user.getHall().getId();
            if (request.getParameter("h" + user.getId()) != null) {
                newIdHall = Long.parseLong(request.getParameter("h" + user.getId()));
            }
            if (user.getPosition().getId() != newIdPosition) {
                user.setPosition(positionValueRepository.findOne(newIdPosition));
                flage = true;
            }
            if (user.getHall().getId() != newIdHall) {
                user.setHall(hallRepository.findOne(newIdHall));
                flage = true;
            }
            if (flage) {
                forSave.add(user);
            }
        }
        userEventRepository.save(forSave);
        return "redirect:/admin/event?id=" + event.getId();
    }

    @RequestMapping(value = "/event/copy")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String copy(final HttpServletRequest request) {
        final Event event = eventRepository.findOne(Long.parseLong(request.getParameter("event")));
        if (Long.parseLong(request.getParameter("baseEvent")) != -1) {
            final Event baseEvent = eventRepository.findOne(Long.parseLong(request.getParameter("baseEvent")));
            final Map<Long, UserEvent> userEventBase = new HashMap<>();
            for (final UserEvent userEvent : baseEvent.getUsers()) {
                userEventBase.put(userEvent.getUserYear().getId(), userEvent);
            }
            final Set<UserEvent> users = event.getUsers();
            final Set<UserEvent> savedUsers = new HashSet<>();
            for (final UserEvent user : users) {
                final Long form = user.getUserYear().getId();
                if (userEventBase.get(form) != null) {
                    boolean needToSave = false;
                    if (userEventBase.get(form).getHall() != null && !user.getHall().equals(userEventBase.get(form).getHall())) {
                        user.setHall(userEventBase.get(form).getHall());
                        needToSave = true;
                    }
                    if (userEventBase.get(form).getPosition() != null && !user.getPosition().equals(userEventBase.get(form).getPosition())) {
                        user.setPosition(userEventBase.get(form).getPosition());
                        needToSave = true;
                    }
                    if (needToSave) {
                        savedUsers.add(user);
                    }
                }
            }
            userEventRepository.save(savedUsers);
        }
        return "redirect:/admin/event/?id=" + event.getId();
    }

    @RequestMapping(value = "/add")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String addAdmin(final HttpServletRequest request) {
        final Long id = Long.parseLong(request.getParameter("newAdmin"));
        final Role roleAdmin = roleRepository.findByName("ROLE_ADMIN");
        final User user = userRepository.findOne(id);
        user.setRole(roleAdmin);
        userRepository.save(user);
        return "redirect:/admin";
    }

    @RequestMapping(value = "/event/attendance", method = RequestMethod.GET)
    public String attendance(@RequestParam(value = "id") final long id, final Model model, final Authentication authentication) {
        event(id, model, authentication);
        model.addAttribute("attendances", Attendance.values());
        model.addAttribute("attendance", true);
        return "showEvent";
    }

    @RequestMapping(value = "/event/assessments", method = RequestMethod.GET)
    public String assessments(@RequestParam(value = "id") final long id, final Model model, final Authentication authentication) {
        event(id, model, authentication);
        final Event event = eventRepository.findOne(id);
        model.addAttribute("assessment", true);
        model.addAttribute("assessments", event.getAssessments());
        if (!model.containsAttribute("newAssessment")) {
            final UserEventAssessment assessment = new UserEventAssessment();
            assessment.setEvent(event);
            model.addAttribute("newAssessment", new UserEventAssessment());
        }
        return "showEvent";
    }

    @RequestMapping(value = "/event/assessments", method = RequestMethod.POST)
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String setAssessments(final HttpServletRequest request) {
        final Event event = eventRepository.findOne(Long.parseLong(request.getParameter("event")));
        final Set<UserEventAssessment> userEventAssessments = new HashSet<>();
        final Iterable<UserEventAssessment> assessments = event.getAssessments();
        for (final UserEventAssessment assessment : assessments) {
            final int val = Integer.parseInt(request.getParameter("assessmentValue" + assessment.getId()));
            if (assessment.getValue() != val) {
                assessment.setValue(val);
                userEventAssessments.add(assessment);
            }
        }
        userEventAssessmentRepository.save(userEventAssessments);


        final Set<UserEvent> users = new HashSet<>();
        for (final UserEvent user : event.getUsers()) {
            userEventRepository.save(user);
            final Set<UserEventAssessment> assessmentSet = new HashSet<>();
            for (final UserEventAssessment assessment : assessments) {
                final boolean chosen = request.getParameter("assessment" + assessment.getId() + "user" + user.getId()) != null;
                if (chosen) {
                    assessmentSet.add(assessment);
                }
            }
            if (!CollectionUtils.isEqualCollection(user.getAssessments(), assessmentSet)) {
                user.getAssessments().clear();
                user.getAssessments().addAll(assessmentSet);
                users.add(user);
            }
        }
        if (!users.isEmpty()) {
            userEventRepository.save(users);
        }
        return "redirect:/admin/event?id=" + request.getParameter("event");
    }

    @RequestMapping(value = "/event/assessments/add", method = RequestMethod.POST)
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String addAttendance(@Valid @ModelAttribute("newAssessment") final UserEventAssessment assessment, final BindingResult result, final RedirectAttributes attributes, final HttpServletRequest request) {
        if (result.hasErrors()) {
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.newAssessment", result);
            attributes.addFlashAttribute("newAssessment", assessment);
        } else {
            userEventAssessmentRepository.save(assessment);
        }
        return "redirect:/admin/event/assessments?id=" + request.getParameter("event");
    }

    @RequestMapping(value = "/event/attendance", method = RequestMethod.POST)
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String setAttendance(final HttpServletRequest request) {
        final Event event = eventRepository.findOne(Long.parseLong(request.getParameter("event")));
        final Set<UserEvent> users = new HashSet<>();
        for (final UserEvent user : event.getUsers()) {
            final String val = request.getParameter("attendance" + user.getId());
            if (!val.equals("NONE")) {
                user.setAttendance(Attendance.valueOf(val));
                users.add(user);
            }
        }
        userEventRepository.save(users);
        return "redirect:/admin/event?id=" + request.getParameter("event");
    }

    @RequestMapping(value = "/medals")
    public String medals(final Model model, final Authentication authentication) {
        setModel(model, getUser(authentication).getYear());
        model.addAttribute("medals", medalRepository.findAll());
        if (!model.containsAttribute("newMedal")) {
            model.addAttribute("newMedal", new Medal());
        }
        return "medals";
    }

    @RequestMapping(value = "/medals/add", method = RequestMethod.POST)
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String addMedals(@Valid @ModelAttribute("newMedal") final Medal medal, final BindingResult result, final RedirectAttributes attributes) {
        if (result.hasErrors()) {
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.newMedal", result);
            attributes.addFlashAttribute("newMedal", medal);
        } else {
            medalRepository.save(medal);
        }
        return "redirect:/admin/medals";
    }

    @RequestMapping(value = "/medals/delete")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String deleteMedal(@RequestParam("id") final long id) {
        medalRepository.delete(id);
        return "redirect:/admin/medals";
    }

    @RequestMapping(value = "/results", method = RequestMethod.GET)
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String showResults(final Model model, final Authentication authentication, final Locale locale) {
        final Year year = getUser(authentication).getYear();
        final Set<ApplicationForm> users = year.getUsers();
        final Set<ApplicationForm> needToSave = new HashSet<>();
        final int countEvents = year.getEvents().size();
        final Map<Long, Integer> assessments = new HashMap<>();
        final Map<Long, Double> experience = new HashMap<>();
        for (final ApplicationForm user : users) {
            double exp = 0;
            double totalExp = 0;
            final int[] assessment = {0};
            for (final UserEvent userEvent : user.getUserEvents()) {
                if (userEvent.getAttendance() == Attendance.YES || userEvent.getAttendance() == Attendance.LATE) {
                    exp += userEvent.getPosition().getValue() / countEvents;
                }
                userEvent.getAssessments().forEach(
                        userEventAssessment -> assessment[0] += userEventAssessment.getValue());
            }
            for (final ApplicationForm applicationForm : user.getUser().getApplicationForms()) {
                totalExp += applicationForm.getExperience();
            }
            totalExp -= user.getExperience();
            totalExp += exp;
            if (exp != user.getExperience()) {
                user.setExperience(exp);
                needToSave.add(user);
            }
            assessments.put(user.getId(), assessment[0]);
            experience.put(user.getId(), totalExp);
        }
        applicationFormRepository.save(needToSave);
        final List<ApplicationForm> applicationForms = new ArrayList<>(users);
        applicationForms.sort(
                (user1, user2) -> {
                    if (experience.get(user1.getId()).equals(experience.get(user2.getId()))) {
                        return Integer.compare(assessments.get(user2.getId()), assessments.get(user1.getId()));
                    } else {
                        return Double.compare(experience.get(user2.getId()), experience.get(user1.getId()));
                    }
                }
        );
        final Map<Long, Medal> userMedals = new HashMap<>();
        final List<Medal> medals = new ArrayList<>(medalRepository.findAll());
        medals.sort(Comparator.comparing(Medal::getValue).reversed());
        medals.add(new Medal(messageSource.getMessage("volunteers.results.noMedal", null, "No medal", locale), -1));
        for (int i = 0, j = 0; i < applicationForms.size(); i++) {
            while (medals.get(j).getValue() > experience.get(applicationForms.get(i).getId())) {
                j++;
            }
            userMedals.put(applicationForms.get(i).getId(), medals.get(j));
        }
        setModel(model, year);
        model.addAttribute("applicationForms", applicationForms);
        model.addAttribute("assessments", assessments);
        model.addAttribute("experience", experience);
        model.addAttribute("medals", userMedals);
        return "results";
    }

    private User getUser(final Authentication authentication) {
        return userRepository.findByEmailIgnoreCase(authentication.getName());
    }

    private void setModel(final Model model, final Year year) {
        model.addAttribute("year", year);
        if (!model.containsAttribute("newYear")) {
            model.addAttribute("newYear", new Year());
        }
        model.addAttribute("years", yearRepository.findAll());
        if (year != null) {
            model.addAttribute("events", year.getEvents());
            model.addAttribute("positions", year.getPositionValues());
            final Set<Hall> halls = year.getHalls();
            halls.add(hallRepository.findOne(1L));
            model.addAttribute("halls", halls);
        } else {
            model.addAttribute("events", Collections.EMPTY_LIST);
            model.addAttribute("positions", Collections.EMPTY_LIST);
            model.addAttribute("halls", Collections.EMPTY_LIST);
        }
        if (!model.containsAttribute("newEvent")) {
            final Event newEvent = new Event();
            newEvent.setYear(year);
            model.addAttribute("newEvent", newEvent);
        }
        if (!model.containsAttribute("newPosition")) {
            model.addAttribute("newPosition", new PositionForm());
        }
        if (!model.containsAttribute("newHall")) {
            final Hall newHall = new Hall();
            newHall.setYear(year);
            model.addAttribute("newHall", newHall);
        }
        final Role roleUser = roleRepository.findByName("ROLE_USER");
        final Role roleAdmin = roleRepository.findByName("ROLE_ADMIN");
        model.addAttribute("roleAdmin", roleAdmin.getUsers());
        model.addAttribute("roleUsers", roleUser.getUsers());
    }


}

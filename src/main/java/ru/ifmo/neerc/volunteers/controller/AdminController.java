package ru.ifmo.neerc.volunteers.controller;

import lombok.AllArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.spring.support.Layout;
import org.thymeleaf.util.StringUtils;
import ru.ifmo.neerc.volunteers.entity.*;
import ru.ifmo.neerc.volunteers.form.HallForm;
import ru.ifmo.neerc.volunteers.form.PositionForm;
import ru.ifmo.neerc.volunteers.modal.JsonResponse;
import ru.ifmo.neerc.volunteers.modal.Status;
import ru.ifmo.neerc.volunteers.repository.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created by Lapenok Akesej on 25.02.2017.
 */
@RequestMapping("/admin")
@Controller
@Layout("publicAdmin")
@EnableTransactionManagement
@AllArgsConstructor
public class AdminController {

    private final YearRepository yearRepository;
    private final EventRepository eventRepository;
    private final HallRepository hallRepository;
    private final UserEventRepository userEventRepository;
    private final UserRepository userRepository;
    private final PositionValueRepository positionValueRepository;
    private final RoleRepository roleRepository;
    private final UserEventAssessmentRepository userEventAssessmentRepository;
    private final MedalRepository medalRepository;
    private final ApplicationFormRepository applicationFormRepository;
    private final MessageSource messageSource;
    private final Locale locale = LocaleContextHolder.getLocale();

    @GetMapping
    public String admin(final Model model, final Authentication authentication) {
        final User user = getUser(authentication);
        if (user.getYear() != null) {
            return "redirect:/admin/year/" + user.getYear().getId();
        }
        final List<Year> years = yearRepository.findAll();
        if (years.size() != 0) {
            return "redirect:/admin/year/" + years.get(years.size() - 1).getId();
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

    @PostMapping("/position/add")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public @ResponseBody
    JsonResponse addPosition(@Valid @ModelAttribute("newPosition") final PositionForm positionForm, final BindingResult result, /*final RedirectAttributes attributes,*/ final Authentication authentication) {
        JsonResponse response = new JsonResponse();
        if (result.hasErrors()) {
            response.setStatus(Status.FAIL);
            response.setResult(result.getAllErrors());
            /*attributes.addFlashAttribute("org.springframework.validation.BindingResult.newPosition", result);
            attributes.addFlashAttribute("newPosition", positionForm);*/
        } else {
            final User user = getUser(authentication);
            final Year year = user.getYear();
            final PositionValue positionValue = new PositionValue(positionForm, year);
            positionValueRepository.save(positionValue);
            response.setStatus(Status.OK);
            response.setResult(positionValue);
        }
        return response;
    }

    @PostMapping("/position/value")
    public @ResponseBody
    JsonResponse setPositionValue(@RequestParam final long id, @RequestParam final double value) {
        JsonResponse response = new JsonResponse();
        try {
            PositionValue positionValue = positionValueRepository.findOne(id);
            if (positionValue.getValue() != value) {
                positionValue.setValue(value);
                positionValueRepository.save(positionValue);
            }
            response.setStatus(Status.OK);
        } catch (Exception e) {
            response.setStatus(Status.FAIL);
        }
        return response;

    }

    @PostMapping("/position/delete")
    //@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public @ResponseBody
    JsonResponse deletePosition(@RequestParam final long id) {
        final PositionValue position = positionValueRepository.findOne(id);
        JsonResponse result = new JsonResponse();
        if (position != null && !position.isDef()) {
            try {
                positionValueRepository.delete(id);
                result.setStatus(Status.OK);
                result.setResult("");
            } catch (final Exception e) {
                result.setStatus(Status.FAIL);
                result.setResult(messageSource.getMessage("volunteers.position.error.delete", new Object[]{position.getName()}, "Error to delete position", locale));
            }
        } else {
            result.setStatus(Status.FAIL);
            result.setResult("Error!!!");
        }
        return result;
    }

    @PostMapping("/hall/delete")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public @ResponseBody
    JsonResponse deleteHall(@RequestParam final long id) {
        JsonResponse response = new JsonResponse();
        try {
            if (!hallRepository.findOne(id).isDef()) {
                hallRepository.delete(id);
                response.setStatus(Status.OK);
            } else {
                response.setStatus(Status.FAIL);
            }
        } catch (final Exception e) {
            final Hall hall = hallRepository.findOne(id);
            response.setStatus(Status.FAIL);
            response.setResult(messageSource.getMessage("volunteers.hall.error.delete", new Object[]{hall.getName()}, "Error to delete hall", locale));
        }
        return response;
    }

    @GetMapping("/hall")
    public String hall(final Model model, final Authentication authentication) {
        setModel(model, getUser(authentication).getYear());
        model.addAttribute("title", "Halls");
        return "hall";
    }

    @PostMapping("hall/edit")
    public @ResponseBody
    JsonResponse editHall(@RequestParam final long id, @RequestParam final String name, @RequestParam final String description) {
        JsonResponse response = new JsonResponse();
        try {
            Hall hall = hallRepository.findOne(id);
            boolean isChanged = false;
            if (!description.isEmpty() && !hall.getDescription().equals(description)) {
                hall.setDescription(description);
                isChanged = true;
            }
            if (!name.isEmpty() && !hall.getName().equals(name)) {
                hall.setName(name);
                isChanged = true;
            }
            if (isChanged) {
                hallRepository.save(hall);
            }
            response.setStatus(Status.OK);
        } catch (Exception e) {
            response.setStatus(Status.FAIL);
            response.setResult(e.getMessage());
        }
        return response;
    }

    @PostMapping("/hall/add")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public @ResponseBody
    JsonResponse addHall(@Valid @ModelAttribute("newHall") final HallForm hall, final BindingResult result, final Authentication authentication) {
        JsonResponse response = new JsonResponse();
        if (result.hasErrors()) {
            response.setStatus(Status.FAIL);
            response.setResult(result.getAllErrors());
        } else {
            final Year year = getUser(authentication).getYear();
            Hall newHall = new Hall(hall, year);
            hallRepository.save(newHall);
            response.setStatus(Status.OK);
            response.setResult(newHall);
        }
        return response;
    }

    @PostMapping("/year/add")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String addYear(@Valid @ModelAttribute("newYear") final Year year, final BindingResult result, final RedirectAttributes attributes, final Authentication authentication) {
        final Year yearOld = getUser(authentication).getYear();
        if (result.hasErrors()) {
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.newYear", result);
            attributes.addFlashAttribute("newYear", year);
            if (yearOld != null) {
                return "redirect:/admin/year/" + yearOld.getId();
            } else {
                return "redirect:/admin";
            }
        }
        year.setOpenForRegistration(false);
        yearRepository.save(year);
        /*if (yearOld != null) {
            final Set<PositionValue> positionValuesOld = yearOld.getPositionValues();
            final Map<Long, Double> positionValueMap = new HashMap<>();
            for (final PositionValue positionValue : positionValuesOld) {
                positionValueMap.put(positionValue.getId(), positionValue.getValue());
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
        }*/
        return "redirect:/admin/year/" + year.getId();
    }

    @PostMapping("/year/close")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String closeYear(@RequestParam final long id, @RequestParam final boolean isOpen) {

        final Year year = yearRepository.findOne(id);
        year.setOpenForRegistration(isOpen);
        yearRepository.save(year);
        return "redirect:/admin/year/" + year.getId();
    }

    @GetMapping("/year/{id}")
    public String showYear(@PathVariable final long id, final Model model, final Authentication authentication) {
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

    @PostMapping("/event/add")
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
                return "redirect:/admin/year/" + year.getId();
            } else {
                return "redirect:/admin";
            }
        }
        eventRepository.save(event);
        final Set<ApplicationForm> users = year.getUsers();
        event.setUsers(new HashSet<>());
        final PositionValue positionValue = findOrCreateDefaultPosition(year);
        final Hall hall = findOrCreateDefaultHall(year);
        final List<UserEvent> userEvents = new ArrayList<>();
        users.forEach(applicationForm -> {
            final UserEvent userEvent = new UserEvent();
            userEvent.setEvent(event);
            userEvent.setHall(hall);
            userEvent.setPosition(positionValue);
            userEvent.setUserYear(applicationForm);
            userEvents.add(userEvent);
        });
        userEventRepository.save(userEvents);
        return "redirect:/admin/event/" + event.getId() + "/";
    }

    private PositionValue findOrCreateDefaultPosition(final Year year) {
        PositionValue positionValue = null;
        for (final PositionValue positionValue1 : year.getPositionValues()) {
            if (positionValue1.isDef()) {
                positionValue = positionValue1;
            }
        }
        if (positionValue == null) {
            positionValue = new PositionValue(messageSource.getMessage("volunteers.reserve.position", null, "Reserve", locale), true, 0, year);
            positionValueRepository.save(positionValue);
        }
        return positionValue;
    }

    private Hall findOrCreateDefaultHall(final Year year) {
        Hall hall = null;
        for (final Hall hall1 : year.getHalls()) {
            if (hall1.isDef())
                hall = hall1;
        }
        if (hall == null) {
            hall = new Hall(messageSource.getMessage("volunteers.reserve.hall", null, "Reserve", locale), true, "", year);
            hallRepository.save(hall);
        }
        return hall;
    }

    @GetMapping("event/{id}")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String event(@PathVariable(value = "id") final long id, final Model model, final Authentication authentication) {
        final Year year = getUser(authentication).getYear();
        final Event currentEvent = eventRepository.findOne(id);
        setModel(model, year);
        final Set<ApplicationForm> yearUsers = new HashSet<>(year.getUsers());
        yearUsers.removeAll(currentEvent.getUsers().stream().map(UserEvent::getUserYear).collect(Collectors.toSet()));
        final Hall reserve = findOrCreateDefaultHall(year);

        final PositionValue defaultPosition = findOrCreateDefaultPosition(year);

        userEventRepository.save(yearUsers.stream().map(af -> {
            final UserEvent ue = new UserEvent();
            ue.setUserYear(af);
            ue.setHall(reserve);
            ue.setPosition(defaultPosition);
            currentEvent.addUser(ue);
            return ue;
        }).collect(Collectors.toList()));

        final Event event = eventRepository.save(currentEvent);


        final HashMap<Hall, List<UserEvent>> hallUser = new HashMap<>(
                event.getUsers().stream().collect(Collectors.groupingBy(UserEvent::getHall)));
        hallUser.forEach((u, v) -> v.sort(Comparator.comparing(lst -> lst.getPosition().getName())));


        final Set<Hall> halls = year.getHalls();
        hallUser.putAll(halls.stream()
                .filter(h -> !hallUser.containsKey(h))
                .collect(Collectors.toMap(Function.identity(), hall -> new ArrayList<>())));

        model.addAttribute("hallUser", hallUser);
        model.addAttribute("event", event);
        model.addAttribute("halls", halls);
        model.addAttribute("title", event.getName());

        Map<Attendance, String> attendanceMap = getAttendaceMap();
        model.addAttribute("attendanceMap", attendanceMap);
        return "showEvent";
    }

    private Map<Attendance, String> getAttendaceMap() {
        return new HashMap<>(Arrays.stream(Attendance.values())
                .collect(Collectors.toMap(Function.identity(), attendance -> messageSource.getMessage("volunteers.attendance." + attendance.name().toLowerCase(), null, attendance.name(), locale))));
    }

    @GetMapping("/event/{id}/edit")
    public String editEvent(@PathVariable(value = "id") final long id, final Model model, final Authentication authentication) {
        final Year year = getUser(authentication).getYear();
        final Event event = eventRepository.findOne(id);
        setModel(model, year);
        model.addAttribute("event", event);
        model.addAttribute("users", event.getUsers());
        return "event";
    }

    @PostMapping("/event/save")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public @ResponseBody
    JsonResponse save(@RequestParam final long userId, @RequestParam final long hallId, @RequestParam final long positionId) {
        JsonResponse response = new JsonResponse();
        try {
            UserEvent user = userEventRepository.findOne(userId);
            PositionValue positionValue = positionId == -1 ? user.getPosition() : positionValueRepository.findOne(positionId);
            Hall hall = hallId == -1 ? user.getHall() : hallRepository.findOne(hallId);
            boolean isChanged = false;
            if (!user.getHall().equals(hall)) {
                user.setHall(hall);
                isChanged = true;
            }
            if (!user.getPosition().equals(positionValue)) {
                user.setPosition(positionValue);
                isChanged = true;
            }
            if (isChanged) {
                userEventRepository.save(user);
            }
            response.setStatus(Status.OK);
        } catch (Exception e) {
            response.setResult(e.getMessage());
            response.setStatus(Status.FAIL);
        }
        return response;
    }

    @PostMapping("/event/copy")
    public @ResponseBody
    JsonResponse copy(@RequestParam final long eventId, @RequestParam final long baseEventId) {
        JsonResponse response = new JsonResponse();
        try {
            final Event event = eventRepository.findOne(eventId);
            final Event baseEvent = eventRepository.findOne(baseEventId);
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
            response.setStatus(Status.OK);
            response.setResult(eventRepository.findOne(eventId));
        } catch (Exception e) {
            response.setStatus(Status.FAIL);
            response.setResult(e.getMessage());
        }
        return response;
    }

    @PostMapping("/add")
    public @ResponseBody
    JsonResponse addAdmin(@RequestParam final long userId) {
        JsonResponse response = new JsonResponse();
        try {
            final Role roleAdmin = roleRepository.findByName("ROLE_ADMIN");
            final User user = userRepository.findOne(userId);
            user.setRole(roleAdmin);
            userRepository.save(user);
            response.setStatus(Status.OK);
        } catch (Exception e) {
            response.setStatus(Status.FAIL);
            response.setResult(e.getMessage());
        }
        return response;
    }

    @GetMapping("/event/{id}/attendance")
    public String attendance(@PathVariable(value = "id") final long id, final Model model, final Authentication authentication) {
        event(id, model, authentication);
        model.addAttribute("attendances", Attendance.values());
        model.addAttribute("attendance", true);
        return "showEvent";
    }

    @GetMapping("/event/{id}/assessments")
    public String assessments(@PathVariable(value = "id") final long id, final Model model, final Authentication authentication) {
        event(id, model, authentication);
        final Event event = eventRepository.findOne(id);
        model.addAttribute("assessment", true);
        model.addAttribute("assessments", event.getAssessments());
        if (!model.containsAttribute("newAssessment")) {
            final Assessment assessment = new Assessment();
            assessment.setEvent(event);
            model.addAttribute("newAssessment", new Assessment());
        }
        return "showEvent";
    }

    @PostMapping("/event/assessments")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public @ResponseBody
    JsonResponse setAssessments(@Valid @ModelAttribute("newAssessment") final Assessment assessment, final BindingResult result, @RequestParam final long userId) {
        JsonResponse response = new JsonResponse();
        try {
            if (result.hasErrors()) {
                response.setStatus(Status.FAIL);
                response.setResult(result.getAllErrors());
            }
            UserEvent user = userEventRepository.findOne(userId);
            assessment.setUser(user);
            userEventAssessmentRepository.save(assessment);
            response.setStatus(Status.OK);
            response.setResult(assessment);
        } catch (Exception e) {
            response.setStatus(Status.FAIL);
            response.setResult(e.getMessage());
        }
        return response;
    }

    @PostMapping("/event/assessments/add")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String addAttendance(@Valid @ModelAttribute("newAssessment") final Assessment assessment, final BindingResult result, final RedirectAttributes attributes, final HttpServletRequest request) {
        if (result.hasErrors()) {
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.newAssessment", result);
            attributes.addFlashAttribute("newAssessment", assessment);
        } else {
            userEventAssessmentRepository.save(assessment);
        }
        return "redirect:/admin/event/assessments?id=" + request.getParameter("event");
    }

    @PostMapping("/event/attendance")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public @ResponseBody
    JsonResponse setAttendance(@RequestParam final long id, @RequestParam final String value) {
        JsonResponse response = new JsonResponse();
        try {
            UserEvent user = userEventRepository.findOne(id);
            user.setAttendance(Attendance.valueOf(value));
            userEventRepository.save(user);
            response.setStatus(Status.OK);
        } catch (Exception e) {
            response.setStatus(Status.FAIL);
            response.setResult(e.getMessage());
        }
        return response;
    }

    @GetMapping(value = "/medals")
    public String medals(final Model model, final Authentication authentication) {
        setModel(model, getUser(authentication).getYear());
        model.addAttribute("medals", medalRepository.findAll());
        if (!model.containsAttribute("newMedal")) {
            model.addAttribute("newMedal", new Medal());
        }
        return "medals";
    }

    @PostMapping("/medals/add")
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

    @GetMapping("/medals/delete")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String deleteMedal(@RequestParam("id") final long id) {
        medalRepository.delete(id);
        return "redirect:/admin/medals";
    }

    @GetMapping("/results")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String showResults(final Model model, final Authentication authentication, final Locale locale) {
        final Year year = getUser(authentication).getYear();
        final Set<ApplicationForm> users = year.getUsers();
        final Set<ApplicationForm> needToSave = new HashSet<>();
        final int countEvents = year.getEvents().size();
        final Map<Long, Integer> assessments = new HashMap<>();
        final Map<Long, List<String>> assessmentsGroupByDays = new HashMap<>();
        final Map<Long, Double> experience = new HashMap<>();
        final Map<ApplicationForm, Set<Hall>> halls = new HashMap<>();
        for (final ApplicationForm user : users) {
            double exp = 0;
            double totalExp = 0;
            final int[] assessment = {0};
            assessmentsGroupByDays.put(user.getId(), new ArrayList<>());
            halls.put(user, new HashSet<>());
            for (final UserEvent userEvent : user.getUserEvents()) {
                if (userEvent.getAttendance() == Attendance.YES || userEvent.getAttendance() == Attendance.LATE) {
                    exp += userEvent.getPosition().getValue() / countEvents;
                }

                Set<Assessment> allAssessments = new HashSet<>(userEvent.getAssessments());
                allAssessments.add(getAssessmentByAttendace(userEvent.getAttendance(), userEvent.getEvent()));

                halls.get(user).add(userEvent.getHall());
                allAssessments.forEach(
                        userEventAssessment -> assessment[0] += userEventAssessment.getValue());

                String str = StringUtils.join(allAssessments.stream().map(Assessment::getValue).collect(Collectors.toList()), ", ");
                assessmentsGroupByDays.get(user.getId()).add("(" + str + ")");
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
        model.addAttribute("assessmentsGroupByDays", assessmentsGroupByDays);
        model.addAttribute("experience", experience);
        model.addAttribute("medals", userMedals);
        model.addAttribute("halls", halls);
        return "results";
    }

    private Assessment getAssessmentByAttendace(Attendance attendance, Event event) {
        Assessment back = new Assessment();
        Map<Attendance, String> attendanceMap = getAttendaceMap();
        back.setComment(event.getName() + " (" + attendanceMap.get(attendance) + ")");
        switch (attendance) {
            case YES:
                back.setValue(event.getAttendanceValue());
                break;
            case LATE:
                back.setValue(event.getAttendanceValue() / 2);
                break;
            case NO:
                back.setValue(-event.getAttendanceValue());
                break;
            case SICK:
                back.setValue(0);
                break;
        }
        return back;
    }

    @GetMapping("/results/user/{id}")
    public String detailedResultUser(@PathVariable final long id, final Model model, final Authentication authentication) {
        ApplicationForm applicationForm = applicationFormRepository.findOne(id);
        List<Assessment> assessments = new ArrayList<>();
        applicationForm.getUserEvents().forEach(user -> {
            assessments.addAll(user.getAssessments());
            assessments.add(getAssessmentByAttendace(user.getAttendance(), user.getEvent()));
        });

        setModel(model, getUser(authentication).getYear());
        model.addAttribute("table", assessments);
        return "detailedResult";
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
            model.addAttribute("halls", year.getHalls());
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
            model.addAttribute("newHall", new HallForm());
        }
        final Role roleUser = roleRepository.findByName("ROLE_USER");
        final Role roleAdmin = roleRepository.findByName("ROLE_ADMIN");
        model.addAttribute("roleAdmin", roleAdmin.getUsers());
        model.addAttribute("roleUsers", roleUser.getUsers());
    }
}

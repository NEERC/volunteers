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
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.spring.support.Layout;
import org.thymeleaf.util.StringUtils;
import ru.ifmo.neerc.dev.Pair;
import ru.ifmo.neerc.volunteers.entity.*;
import ru.ifmo.neerc.volunteers.form.HallForm;
import ru.ifmo.neerc.volunteers.form.MailForm;
import ru.ifmo.neerc.volunteers.form.PositionForm;
import ru.ifmo.neerc.volunteers.modal.JsonResponse;
import ru.ifmo.neerc.volunteers.modal.Status;
import ru.ifmo.neerc.volunteers.repository.*;
import ru.ifmo.neerc.volunteers.service.Utils;
import ru.ifmo.neerc.volunteers.service.day.DayService;
import ru.ifmo.neerc.volunteers.service.mail.EmailService;
import ru.ifmo.neerc.volunteers.service.user.UserService;
import ru.ifmo.neerc.volunteers.service.year.YearService;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.IOException;
import java.net.URL;
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
    private final DayRepository dayRepository;
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
    private final EmailService emailService;

    private final UserService userService;
    private final YearService yearService;
    private final DayService dayService;
    private final Utils utils;

    @GetMapping
    public String admin(final Model model, final Authentication authentication) {
        final User user = userService.getUserByAuthentication(authentication);
        Optional<Year> currentYear = Optional.ofNullable(user.getYear());
        if (!currentYear.isPresent()) {
            currentYear = yearService.getLastYear();
        }
        if (currentYear.isPresent()) {
            return "redirect:/admin/year/" + currentYear.get().getId();
        }
        utils.setModelForAdmin(model, null);
        return "admin";
    }

    @GetMapping("/position")
    public String positions(final Model model, final Authentication authentication) {
        utils.setModelForAdmin(model, userService.getUserByAuthentication(authentication));
        model.addAttribute("title", "Positions");
        return "position";
    }

    @PostMapping("/position/add")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public @ResponseBody
    JsonResponse addPosition(@Valid @ModelAttribute("newPosition") final PositionForm positionForm, final BindingResult result, final Authentication authentication) {

        if (result.hasErrors()) {
            JsonResponse<List<ObjectError>> response = new JsonResponse<>();
            response.setStatus(Status.FAIL);
            response.setResult(result.getAllErrors());
            return response;
            /*attributes.addFlashAttribute("org.springframework.validation.BindingResult.newPosition", result);
            attributes.addFlashAttribute("newPosition", positionForm);*/
        } else {
            JsonResponse<PositionValue> response = new JsonResponse<>();
            final User user = userService.getUserByAuthentication(authentication);
            final Year year = user.getYear();
            final PositionValue positionValue = new PositionValue(positionForm, year);
            positionValueRepository.save(positionValue);
            response.setStatus(Status.OK);
            response.setResult(positionValue);
            return response;
        }
    }

    @PostMapping("/position/value")
    public @ResponseBody
    JsonResponse<Exception> setPositionValue(@RequestParam final long id, @RequestParam final double value) {
        JsonResponse<Exception> response = new JsonResponse<>();
        try {
            PositionValue positionValue = positionValueRepository.findOne(id);
            if (positionValue.getValue() != value) {
                positionValue.setValue(value);
                positionValueRepository.save(positionValue);
            }
            response.setStatus(Status.OK);
        } catch (Exception e) {
            response.setStatus(Status.FAIL);
            response.setResult(e);
        }
        return response;

    }

    @PostMapping("/position/order")
    public @ResponseBody
    JsonResponse<Exception> setPositionOrder(@RequestParam final long id, @RequestParam final long order) {
        JsonResponse<Exception> response = new JsonResponse<>();
        try {
            PositionValue positionValue = positionValueRepository.findOne(id);
            if (positionValue.getOrd() != order) {
                positionValue.setOrd(order);
                positionValueRepository.save(positionValue);
            }
            response.setStatus(Status.OK);
        } catch (Exception e) {
            response.setStatus(Status.FAIL);
            response.setResult(e);
        }
        return response;
    }

    @PostMapping("/position/inform")
    public @ResponseBody
    JsonResponse<Exception> setPositionInForm(@RequestParam final long id, @RequestParam final boolean inForm) {
        JsonResponse<Exception> response = new JsonResponse<>();
        try {
            PositionValue positionValue = positionValueRepository.findOne(id);
            if (positionValue.isInForm() != inForm) {
                positionValue.setInForm(inForm);
                positionValueRepository.save(positionValue);
            }
            response.setStatus(Status.OK);
        } catch (Exception e) {
            response.setStatus(Status.FAIL);
            response.setResult(e);
        }
        return response;
    }

    @PostMapping("/position/delete")
    //@Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public @ResponseBody
    JsonResponse deletePosition(@RequestParam final long id) {
        final PositionValue position = positionValueRepository.findOne(id);
        JsonResponse<String> result = new JsonResponse<>();
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
        JsonResponse<String> response = new JsonResponse<>();
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
        utils.setModelForAdmin(model, userService.getUserByAuthentication(authentication));
        model.addAttribute("title", "Halls");
        return "hall";
    }

    @PostMapping("hall/edit")
    public @ResponseBody
    JsonResponse editHall(@RequestParam final long id, @RequestParam final String name, @RequestParam final String description) {
        JsonResponse<String> response = new JsonResponse<>();
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

        if (result.hasErrors()) {
            JsonResponse<List<ObjectError>> response = new JsonResponse<>();
            response.setStatus(Status.FAIL);
            response.setResult(result.getAllErrors());
            return response;
        } else {
            JsonResponse<Hall> response = new JsonResponse<>();
            final Year year = userService.getUserByAuthentication(authentication).getYear();
            Hall newHall = new Hall(hall, year);
            hallRepository.save(newHall);
            response.setStatus(Status.OK);
            response.setResult(newHall);
            return response;
        }
    }

    @PostMapping("/year/add")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String addYear(@Valid @ModelAttribute("newYear") final Year year, final BindingResult result, final RedirectAttributes attributes, final Authentication authentication) {
        final Year yearOld = userService.getUserByAuthentication(authentication).getYear();
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
        return "redirect:/admin/year/" + year.getId();
    }

    @PostMapping("/year/close")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public @ResponseBody
    JsonResponse closeYear(@RequestParam final long id, @RequestParam final boolean isOpen) {
        JsonResponse<String> response = new JsonResponse<>();
        try {
            final Year year = yearRepository.findOne(id);
            year.setOpenForRegistration(isOpen);
            yearRepository.save(year);
            response.setStatus(Status.OK);
        } catch (Exception e) {
            response.setStatus(Status.FAIL);
            response.setResult(e.getMessage());
        }
        return response;
    }

    @GetMapping("/year/{id}")
    public String showYear(@PathVariable final long id, final Model model, final Authentication authentication) {
        final User user = userService.getUserByAuthentication(authentication);

        final Year year = yearRepository.findOne(id);
        userService.setUserYear(user, year);
        utils.setModelForAdmin(model, user);
        final Set<ApplicationForm> users = year.getUsers();
        model.addAttribute("users", users);
        /*if (!model.containsAttribute("day")) {
            Day day = new Day();
            day.setYear(year);
            model.addAttribute("day", day);
        }*/
        model.addAttribute("title", year.getName());
        return "adminHome";
    }

    @PostMapping("/day/edit")
    public @ResponseBody
    JsonResponse<Day> saveEvent(@Valid @ModelAttribute final Day day, final BindingResult result, final Authentication authentication) {
        JsonResponse<Day> response = new JsonResponse<>();
        final User user = userService.getUserByAuthentication(authentication);
        Year year = day.getYear();
        if (year == null) {
            year = user.getYear();
            day.setYear(year);
        }
        if (result.hasErrors()) {
            if (year != null) {
                response.setStatus(Status.FAIL);
                response.setResult(day);
            } else {
                response.setStatus(Status.FAIL);
                response.setResult(null);
            }
            return response;
        }
        dayRepository.save(day);
        response.setStatus(Status.OK);
        response.setResult(day);
        return response;
    }

    @PostMapping("/day/add")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String addEvent(@Valid @ModelAttribute("newDay") final Day dayForm, final BindingResult result, final RedirectAttributes attributes, final Authentication authentication) {
        JsonResponse<Day> response = saveEvent(dayForm, result, authentication);
        if (response.getStatus() == Status.FAIL) {
            if (response.getResult() != null) {
                attributes.addFlashAttribute("org.springframework.validation.BindingResult.newEvent", result);
                attributes.addFlashAttribute("newEvent", dayForm);
                return "redirect:/admin/year/" + response.getResult().getYear().getId();
            }
            return "redirect:/admin/";
        }
        Day day = response.getResult();
        Year year = day.getYear();
        final Set<ApplicationForm> users = year.getUsers();
        day.setUsers(new HashSet<>());
        final PositionValue positionValue = yearService.findOrCreateDefaultPosition(year, locale);
        final Hall hall = yearService.findOrCreateDefaultHall(year, locale);
        final List<UserDay> userDays = new ArrayList<>();
        users.forEach(applicationForm -> {
            final UserDay userDay = new UserDay();
            userDay.setDay(day);
            userDay.setHall(hall);
            userDay.setPosition(positionValue);
            userDay.setUserYear(applicationForm);
            userDays.add(userDay);
        });
        userEventRepository.save(userDays);
        return "redirect:/admin/day/" + day.getId() + "/";
    }


    @GetMapping("day/{id}")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String event(@PathVariable(value = "id") final long id, final Model model, final Authentication authentication) {
        final User user = userService.getUserByAuthentication(authentication);
        final Year year = user.getYear();
        final Day currentDay = dayRepository.findOne(id);
        utils.setModelForAdmin(model, user);

        final HashMap<Hall, List<UserDay>> hallUser = dayService.getHallUser(currentDay, locale);

        final Set<Hall> halls = year.getHalls();

        model.addAttribute("hallUser", hallUser);
        model.addAttribute("day", currentDay);
        model.addAttribute("halls", halls);
        model.addAttribute("title", currentDay.getName());

        Map<Attendance, String> attendanceMap = getAttendaceMap();
        model.addAttribute("attendanceMap", attendanceMap);
        return "showEvent";
    }

    private Map<Attendance, String> getAttendaceMap() {
        return new HashMap<>(Arrays.stream(Attendance.values())
                .collect(Collectors.toMap(Function.identity(), attendance -> messageSource.getMessage("volunteers.attendance." + attendance.name().toLowerCase(), null, attendance.name(), locale))));
    }

    @GetMapping("/day/{id}/edit")
    public String editEvent(@PathVariable(value = "id") final long id, final Model model, final Authentication authentication) {
        final User user = userService.getUserByAuthentication(authentication);
        final Day day = dayRepository.findOne(id);
        utils.setModelForAdmin(model, user);
        model.addAttribute("day", day);
        model.addAttribute("users", day.getUsers());
        final Map<UserDay, Map<Year, Set<Pair<Hall, PositionValue>>>> exp = day.getUsers().stream().collect(Collectors.toMap(Function.identity(),
                u -> u.getUserYear().getUser().getApplicationForms().stream().
                        filter(uy -> !uy.getYear().equals(user.getYear())).
                        collect(Collectors.toMap(ApplicationForm::getYear,
                                uy -> uy.getUserDays().stream().map(ud -> new Pair<>(ud.getHall(), ud.getPosition())).collect(Collectors.toSet())))));
        model.addAttribute("exp", exp);
        return "day";
    }

    @PostMapping("/day/save")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public @ResponseBody
    JsonResponse save(@RequestParam final long userId, @RequestParam final long hallId, @RequestParam final long positionId) {
        JsonResponse<String> response = new JsonResponse<>();
        try {
            UserDay user = userEventRepository.findOne(userId);
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

    @PostMapping("/day/copy")
    public @ResponseBody
    JsonResponse copy(@RequestParam final long eventId, @RequestParam final long baseEventId) {
        try {
            final Day day = dayRepository.findOne(eventId);
            final Day baseDay = dayRepository.findOne(baseEventId);
            final Map<Long, UserDay> userEventBase = new HashMap<>();
            for (final UserDay userDay : baseDay.getUsers()) {
                userEventBase.put(userDay.getUserYear().getId(), userDay);
            }
            final Set<UserDay> users = day.getUsers();
            final Set<UserDay> savedUsers = new HashSet<>();
            for (final UserDay user : users) {
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
            JsonResponse<Day> response = new JsonResponse<>();
            response.setStatus(Status.OK);
            response.setResult(dayRepository.findOne(eventId));
            return response;
        } catch (Exception e) {
            JsonResponse<String> response = new JsonResponse<>();
            response.setStatus(Status.FAIL);
            response.setResult(e.getMessage());
            return response;
        }
    }

    @PostMapping("/add")
    public @ResponseBody
    JsonResponse addAdmin(@RequestParam final long userId) {
        JsonResponse<String> response = new JsonResponse<>();
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

    @GetMapping("/day/{id}/attendance")
    public String attendance(@PathVariable(value = "id") final long id, final Model model, final Authentication authentication) {
        event(id, model, authentication);
        model.addAttribute("attendances", Attendance.values());
        model.addAttribute("attendance", true);
        return "showEvent";
    }

    @GetMapping("/day/{id}/assessments")
    public String assessments(@PathVariable(value = "id") final long id, final Model model, final Authentication authentication) {
        event(id, model, authentication);
        final Day day = dayRepository.findOne(id);
        model.addAttribute("assessment", true);
        model.addAttribute("assessments", day.getUsers().stream().map(UserDay::getAssessments).flatMap(Collection::stream).collect(Collectors.toSet()));
        if (!model.containsAttribute("newAssessment")) {
            model.addAttribute("newAssessment", new Assessment());
        }
        return "showEvent";
    }

    @PostMapping("/day/assessments")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public @ResponseBody
    JsonResponse setAssessments(@Valid @ModelAttribute("newAssessment") final Assessment assessment, final BindingResult result, @RequestParam final long userId) {

        try {
            if (result.hasErrors()) {
                JsonResponse<List<ObjectError>> response = new JsonResponse<>();
                response.setStatus(Status.FAIL);
                response.setResult(result.getAllErrors());
                return response;
            }
            UserDay user = userEventRepository.findOne(userId);
            assessment.setUser(user);
            userEventAssessmentRepository.save(assessment);
            JsonResponse<Assessment> response = new JsonResponse<>();
            response.setStatus(Status.OK);
            response.setResult(assessment);
            return response;
        } catch (Exception e) {
            JsonResponse<String> response = new JsonResponse<>();
            response.setStatus(Status.FAIL);
            response.setResult(e.getMessage());
            return response;
        }
    }

    @PostMapping("/day/attendance")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public @ResponseBody
    JsonResponse setAttendance(@RequestParam final long id, @RequestParam final String value) {
        JsonResponse<String> response = new JsonResponse<>();
        try {
            UserDay user = userEventRepository.findOne(id);
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
        utils.setModelForAdmin(model, userService.getUserByAuthentication(authentication));
        model.addAttribute("medals", medalRepository.findAll());
        if (!model.containsAttribute("newMedal")) {
            model.addAttribute("newMedal", new Medal());
        }
        return "medals";
    }

    @PostMapping("/medals/add")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public @ResponseBody
    JsonResponse addMedals(@Valid @ModelAttribute("newMedal") final Medal medal, final BindingResult result) {
        try {
            if (result.hasErrors()) {
                JsonResponse<List<ObjectError>> response = new JsonResponse<>();
                response.setStatus(Status.FAIL);
                response.setResult(result.getAllErrors());
                return response;
            } else {
                JsonResponse<Medal> response = new JsonResponse<>();
                medalRepository.save(medal);
                response.setResult(medal);
                response.setStatus(Status.OK);
                return response;
            }
        } catch (Exception e) {
            JsonResponse<String> response = new JsonResponse<>();
            response.setResult(e.getMessage());
            response.setStatus(Status.FAIL);
            return response;
        }
    }

    @PostMapping("/medals/delete")
    public @ResponseBody
    JsonResponse deleteMedal(@RequestParam("id") final long id) {
        JsonResponse<String> response = new JsonResponse<>();
        try {
            medalRepository.delete(id);
            response.setStatus(Status.OK);
        } catch (Exception e) {
            response.setStatus(Status.FAIL);
            response.setResult(e.getMessage());
        }
        return response;
    }

    @GetMapping("/results")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String showResults(final Model model, final Authentication authentication, final Locale locale) {
        final Year year = userService.getUserByAuthentication(authentication).getYear();
        final Set<ApplicationForm> users = year.getUsers();
        final Set<ApplicationForm> needToSave = new HashSet<>();
        final int countEvents = year.getDays().size();
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
            for (final UserDay userDay : user.getUserDays()) {
                if (userDay.getAttendance() == Attendance.YES || userDay.getAttendance() == Attendance.LATE) {
                    exp += userDay.getPosition().getValue() / countEvents;
                }

                Set<Assessment> allAssessments = new HashSet<>(userDay.getAssessments());
                allAssessments.add(getAssessmentByAttendace(userDay.getAttendance(), userDay.getDay()));

                halls.get(user).add(userDay.getHall());
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
        utils.setModelForAdmin(model, userService.getUserByAuthentication(authentication));
        model.addAttribute("applicationForms", applicationForms);
        model.addAttribute("assessments", assessments);
        model.addAttribute("assessmentsGroupByDays", assessmentsGroupByDays);
        model.addAttribute("experience", experience);
        model.addAttribute("medals", userMedals);
        model.addAttribute("halls", halls);
        return "results";
    }

    private Assessment getAssessmentByAttendace(Attendance attendance, Day day) {
        Assessment back = new Assessment();
        Map<Attendance, String> attendanceMap = getAttendaceMap();
        back.setComment(day.getName() + " (" + attendanceMap.get(attendance) + ")");
        switch (attendance) {
            case YES:
                back.setValue(day.getAttendanceValue());
                break;
            case LATE:
                back.setValue(day.getAttendanceValue() / 2);
                break;
            case NO:
                back.setValue(-day.getAttendanceValue());
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
        applicationForm.getUserDays().forEach(user -> {
            assessments.addAll(user.getAssessments());
            assessments.add(getAssessmentByAttendace(user.getAttendance(), user.getDay()));
        });

        utils.setModelForAdmin(model, userService.getUserByAuthentication(authentication));
        model.addAttribute("table", assessments);
        return "detailedResult";
    }

    @GetMapping("/events")
    public String getEvents(final Model model, final Authentication authentication, final HttpServletRequest request) throws IOException {
        User user = userService.getUserByAuthentication(authentication);
        Year year = user.getYear();
        utils.setModelForAdmin(model, user);
        String file = year.getCalendar();
        model.addAttribute("file", file);
        URL url = new URL(request.getRequestURL().toString());
        String baseUrl = url.getProtocol() + "://" + url.getHost() + (url.getPort() == 80 || url.getPort() < 0 ? "" : ":" + url.getPort());
        model.addAttribute("baseUrl", baseUrl);
        return "events";
    }

    @PostMapping("/events")
    public @ResponseBody
    JsonResponse editEvents(@RequestParam("file") final String file, Authentication authentication) {
        Year year = userService.getUserByAuthentication(authentication).getYear();
        JsonResponse response = new JsonResponse();
        year.setCalendar(file);
        yearRepository.save(year);
        response.setStatus(Status.OK);
        return response;
    }

    @GetMapping("/email")
    public String sendEmail(final Model model, final Authentication authentication) {
        User user = userService.getUserByAuthentication(authentication);
        utils.setModelForAdmin(model, user);
        model.addAttribute("mailform", new MailForm());
        return "email";
    }

    @PostMapping("/email")
    public String sendEmail(@Valid @ModelAttribute("mailform") final MailForm mailForm, final BindingResult result, Authentication authentication, final Model model) throws MessagingException {
        User user = userService.getUserByAuthentication(authentication);
        if (result.hasErrors()) {
            utils.setModelForAdmin(model, user);
            model.addAttribute("mailform", mailForm);
            return "email";
        }
        emailService.sendSimpleMessage(emailService.constructEmail(mailForm.getSubject(), mailForm.getBody(),
                user.getYear().getUsers().stream().map(ApplicationForm::getUser).toArray(User[]::new)));
        model.addAttribute("result", "ok");
        return "redirect:/admin/year/" + user.getYear().getId();
    }
}

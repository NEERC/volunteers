package ru.ifmo.neerc.volunteers.controller;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import ru.ifmo.neerc.dev.Pair;
import ru.ifmo.neerc.volunteers.entity.*;
import ru.ifmo.neerc.volunteers.form.*;
import ru.ifmo.neerc.volunteers.modal.JsonResponse;
import ru.ifmo.neerc.volunteers.modal.Status;
import ru.ifmo.neerc.volunteers.repository.*;
import ru.ifmo.neerc.volunteers.service.Utils;
import ru.ifmo.neerc.volunteers.service.day.DayService;
import ru.ifmo.neerc.volunteers.service.experience.ExperienceService;
import ru.ifmo.neerc.volunteers.service.mail.EmailService;
import ru.ifmo.neerc.volunteers.service.token.TokenService;
import ru.ifmo.neerc.volunteers.service.user.UserService;
import ru.ifmo.neerc.volunteers.service.year.YearService;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.PrintWriter;
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
@Slf4j
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
    private final TokenService tokenService;
    private final ExperienceService experienceService;
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

    @PostMapping("/position/edit")
    public @ResponseBody
    JsonResponse<Exception> setCurName(@Valid @ModelAttribute("position") final PositionForm positionForm, final BindingResult result, @RequestParam long id) {
        JsonResponse<Exception> response = new JsonResponse<>();
        if (result.hasErrors()) {
            response.setStatus(Status.FAIL);
            response.setResult(new Exception("invalid form"));
            return response;
        }
        try {
            PositionValue positionValue = positionValueRepository.findOne(id);
            positionValue.setFields(positionForm);
            positionValueRepository.save(positionValue);
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
    JsonResponse editHall(@RequestParam final long id, @Valid @ModelAttribute("hall") final HallForm hallForm, final BindingResult result) {
        JsonResponse<String> response = new JsonResponse<>();
        if (result.hasErrors()) {
            response.setResult("Invalid form");
            response.setStatus(Status.FAIL);
        }
        try {
            Hall hall = hallRepository.findOne(id);
            hall.setFields(hallForm);
            hallRepository.save(hall);
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

        Map<Attendance, String> attendanceMap = getAttendanceMap();
        model.addAttribute("attendanceMap", attendanceMap);
        return "showEvent";
    }

    private Map<Attendance, String> getAttendanceMap() {
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
            model.addAttribute("newMedal", new MedalForm());
        }
        return "medals";
    }

    @PostMapping("/medals/add")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public @ResponseBody
    JsonResponse addMedals(@Valid @ModelAttribute("newMedal") final MedalForm medalForm, final BindingResult result) {
        try {
            if (result.hasErrors()) {
                JsonResponse<List<ObjectError>> response = new JsonResponse<>();
                response.setStatus(Status.FAIL);
                response.setResult(result.getAllErrors());
                return response;
            } else {
                JsonResponse<Medal> response = new JsonResponse<>();
                Medal medal = medalRepository.save(new Medal(medalForm));
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

    @PostMapping("/medals/edit")
    public @ResponseBody
    JsonResponse updateMedal(@Valid @ModelAttribute("medal") final MedalForm medalForm, final BindingResult result, @RequestParam("id") final long id) {
        JsonResponse<String> response = new JsonResponse<>();
        if (result.hasErrors()) {
            response.setStatus(Status.FAIL);
            response.setResult("invalid form");
            return response;
        }
        try {
            Medal medal = medalRepository.findOne(id);
            medal.setFields(medalForm);
            medalRepository.save(medal);
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

        utils.setModelForAdmin(model, userService.getUserByAuthentication(authentication));

        Pair<Map<ApplicationForm, Double>, Map<ApplicationForm, List<String>>> assessments = experienceService.getAssessments(year);
        Map<ApplicationForm, Double> experience = experienceService.getExperience(year);
        List<ApplicationForm> applicationForms = experienceService.getApplicationForms(experience, assessments.getKey());

        model.addAttribute("applicationForms", applicationForms);
        model.addAttribute("assessments", assessments.getKey());
        model.addAttribute("assessmentsGroupByDays", assessments.getValue());
        model.addAttribute("experience", experience);
        model.addAttribute("medals", experienceService.getNewMedals(applicationForms, experience));
        model.addAttribute("halls", experienceService.getHalls(year));
        return "results";
    }


    @GetMapping("/results/user/{id}")
    public String detailedResultUser(@PathVariable final long id, final Model model, final Authentication authentication) {
        ApplicationForm applicationForm = applicationFormRepository.findOne(id);
        Map<Attendance, String> attendanceComments = getAttendanceMap();
        Map<Long, Pair<Double, String>> attendance = applicationForm.getUserDays().stream().collect(Collectors.toMap(
                UserDay::getId,
                d -> new Pair<>(d.calcAttendanceScore(), attendanceComments.get(d.getAttendance()))
        ));

        utils.setModelForAdmin(model, userService.getUserByAuthentication(authentication));
        model.addAttribute("assessmentattendance", attendance);
        model.addAttribute("assessmentuser", applicationForm);
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

    @GetMapping("/tokens")
    public String getTokens(final Model model, final Authentication authentication) {
        User user = userService.getUserByAuthentication(authentication);
        utils.setModelForAdmin(model, user);
        model.addAttribute("token", tokenService.getToken());
        return "tokens";
    }

    @DeleteMapping("/tokens")
    public String revokeToken(@RequestParam final String token, final Authentication authentication) {
        tokenService.revokeToken(token);
        User user = userService.getUserByAuthentication(authentication);
        log.info("User {} revoked token {}", user.getEmail(), token);
        return "redirect:/admin/tokens";
    }

    @GetMapping("/users")
    public String getUsers(final Model model, final Authentication authentication) {
        User user = userService.getUserByAuthentication(authentication);
        utils.setModelForAdmin(model, user);
        model.addAttribute("users", userRepository.findAll());
        return "users";
    }

    @GetMapping("/users/{id}")
    public String getUser(@PathVariable final long id, final Model model, final Authentication authentication) {
        utils.setModelForAdmin(model, userService.getUserByAuthentication(authentication));

        if (!model.containsAttribute("editForm")) {
            User user = userRepository.findOne(id);
            model.addAttribute("editForm", new UserEditForm(user));
        }

        return "userEdit";
    }

    @PutMapping("/users/{id}")
    public String editUser(@PathVariable final long id, @Valid @ModelAttribute final UserEditForm editForm, final BindingResult result, final RedirectAttributes attributes, final Model model, final Authentication authentication) {
        if (result.hasErrors()) {
            attributes.addFlashAttribute("org.springframework.validation.BindingResult.editForm", result);
            attributes.addFlashAttribute("editForm", editForm);
        } else {
            User user = userRepository.findOne(id);
            userService.editUser(user, editForm);
            attributes.addFlashAttribute("isUserSaved", true);
        }

        return "redirect:/admin/users/" + id;
    }

    @GetMapping("/day/{id}/csv")
    public void getBadges(final HttpServletResponse response, @PathVariable("id") long id) throws IOException {
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"file.csv\"");
        PrintWriter writer = response.getWriter();
        writer.write("Team,TeamCur,Role,RoleCur,Name,NameCyr,Medal,Stars\n");
        Day day = dayRepository.findOne(id);
        Map<ApplicationForm, Double> exp = experienceService.getExperienceExceptCurrentYear(day.getYear());
        Map<ApplicationForm, Medal> medals = experienceService.getNewMedals(experienceService.getApplicationForms(exp), exp);
        day.getUsers().forEach(u -> {
            User user = u.getUserYear().getUser();
            String stars = new String(new char[(int) medals.get(u.getUserYear()).getStars()]).replace('\0', '٭');
            writer.write(u.getHall().getName() + "," + u.getHall().getCurName() + "," +
                    u.getPosition().getEngName() + "," + u.getPosition().getCurName() + ",\""
                    + user.getFirstName() + "\n" + user.getLastName() + "\",\"" +
                    user.getFirstNameCyr() + "\n" + user.getLastNameCyr() + "\"," +
                    medals.get(u.getUserYear()).getName() + "," + stars + "\n");
        });
        writer.flush();
        writer.close();
    }

    @GetMapping("/stars")
    public String stars(Model model, Authentication authentication) {
        Year year = userService.getUserByAuthentication(authentication).getYear();
        utils.setModelForAdmin(model, userService.getUserByAuthentication(authentication));
        model.addAttribute("medals", experienceService.getMedals(year));
        model.addAttribute("exp", experienceService.getExperienceExceptCurrentYear(year));
        return "star";
    }

    @PostMapping("/default")
    public @ResponseBody
    JsonResponse<String> setDefaultYear(Authentication authentication) {
        Year year = userService.getUserByAuthentication(authentication).getYear();
        JsonResponse<String> result = new JsonResponse<>();
        try {
            Set<User> users = userRepository.findAll();
            users.forEach(it -> it.setYear(year));
            userRepository.save(users);
            result.setStatus(Status.OK);
        } catch (Exception e) {
            result.setStatus(Status.FAIL);
            result.setResult(e.getMessage());
        }
        return result;
    }
}

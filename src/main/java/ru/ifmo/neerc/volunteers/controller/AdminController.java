package ru.ifmo.neerc.volunteers.controller;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.mail.MessagingException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.tuple.Triple;
import org.docx4j.Docx4J;
import org.docx4j.XmlUtils;
import org.docx4j.model.datastorage.migration.VariablePrepare;
import org.docx4j.openpackaging.packages.WordprocessingMLPackage;
import org.docx4j.openpackaging.parts.WordprocessingML.MainDocumentPart;
import org.docx4j.wml.Document;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.data.util.Pair;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.thymeleaf.spring.support.Layout;
import ru.ifmo.neerc.volunteers.entity.ApplicationForm;
import ru.ifmo.neerc.volunteers.entity.AssBoundary;
import ru.ifmo.neerc.volunteers.entity.Assessment;
import ru.ifmo.neerc.volunteers.entity.Attendance;
import ru.ifmo.neerc.volunteers.entity.Day;
import ru.ifmo.neerc.volunteers.entity.Hall;
import ru.ifmo.neerc.volunteers.entity.Medal;
import ru.ifmo.neerc.volunteers.entity.PositionValue;
import ru.ifmo.neerc.volunteers.entity.User;
import ru.ifmo.neerc.volunteers.entity.UserDay;
import ru.ifmo.neerc.volunteers.entity.Year;
import ru.ifmo.neerc.volunteers.form.HallForm;
import ru.ifmo.neerc.volunteers.form.MailForm;
import ru.ifmo.neerc.volunteers.form.MedalForm;
import ru.ifmo.neerc.volunteers.form.PositionForm;
import ru.ifmo.neerc.volunteers.form.UserEditForm;
import ru.ifmo.neerc.volunteers.modal.AssessmentsJson;
import ru.ifmo.neerc.volunteers.modal.JsonResponse;
import ru.ifmo.neerc.volunteers.modal.Status;
import ru.ifmo.neerc.volunteers.repository.ApplicationFormRepository;
import ru.ifmo.neerc.volunteers.repository.AssBoundaryRepository;
import ru.ifmo.neerc.volunteers.repository.DayRepository;
import ru.ifmo.neerc.volunteers.repository.HallRepository;
import ru.ifmo.neerc.volunteers.repository.MedalRepository;
import ru.ifmo.neerc.volunteers.repository.PositionValueRepository;
import ru.ifmo.neerc.volunteers.repository.RoleRepository;
import ru.ifmo.neerc.volunteers.repository.UserEventAssessmentRepository;
import ru.ifmo.neerc.volunteers.repository.UserEventRepository;
import ru.ifmo.neerc.volunteers.repository.UserRepository;
import ru.ifmo.neerc.volunteers.repository.YearRepository;
import ru.ifmo.neerc.volunteers.service.Utils;
import ru.ifmo.neerc.volunteers.service.day.DayService;
import ru.ifmo.neerc.volunteers.service.experience.ExperienceService;
import ru.ifmo.neerc.volunteers.service.mail.EmailService;
import ru.ifmo.neerc.volunteers.service.token.TokenService;
import ru.ifmo.neerc.volunteers.service.user.UserService;
import ru.ifmo.neerc.volunteers.service.year.YearService;

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
    private final AssBoundaryRepository assBoundaryRepository;
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
        utils.setModelForAdmin(model, userService.getUserByAuthentication(authentication).getYear());
        model.addAttribute("title", "Positions");
        return "position";
    }

    @PostMapping("/position/add")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public @ResponseBody JsonResponse addPosition(@Valid @ModelAttribute("newPosition") final PositionForm positionForm, final BindingResult result, final Authentication authentication) {

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
            final Hall defaultHall =
                    Optional.ofNullable(positionForm.getDefaultHallId()).map(hallRepository::findOne).orElse(null);
            final PositionValue positionValue = new PositionValue(positionForm, year, defaultHall);
            positionValueRepository.save(positionValue);
            response.setStatus(Status.OK);
            response.setResult(positionValue);
            return response;
        }
    }

    @PostMapping("/position/edit")
    public @ResponseBody JsonResponse<Exception> setCurName(@Valid @ModelAttribute("position") final PositionForm positionForm, final BindingResult result, @RequestParam long id) {
        JsonResponse<Exception> response = new JsonResponse<>();
        if (result.hasErrors()) {
            response.setStatus(Status.FAIL);
            response.setResult(new Exception("invalid form"));
            return response;
        }
        try {
            PositionValue positionValue = positionValueRepository.findOne(id);
            final Hall defaultHall =
                    Optional.ofNullable(positionForm.getDefaultHallId()).map(hallRepository::findOne).orElse(null);
            positionValue.setFields(positionForm, defaultHall);
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
    public @ResponseBody JsonResponse deletePosition(@RequestParam final long id) {
        final PositionValue position = positionValueRepository.findOne(id);
        JsonResponse<String> result = new JsonResponse<>();
        if (position != null && !position.isDef()) {
            try {
                positionValueRepository.delete(id);
                result.setStatus(Status.OK);
                result.setResult("");
            } catch (final Exception e) {
                result.setStatus(Status.FAIL);
                result.setResult(messageSource.getMessage("volunteers.position.error.delete",
                        new Object[]{position.getName()}, "Error to delete position", locale));
            }
        } else {
            result.setStatus(Status.FAIL);
            result.setResult("Error!!!");
        }
        return result;
    }

    @PostMapping("/hall/delete")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public @ResponseBody JsonResponse deleteHall(@RequestParam final long id) {
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
            response.setResult(messageSource.getMessage("volunteers.hall.error.delete", new Object[]{hall.getName()},
                    "Error to delete hall", locale));
        }
        return response;
    }

    @GetMapping("/hall")
    public String hall(final Model model, final Authentication authentication) {
        utils.setModelForAdmin(model, userService.getUserByAuthentication(authentication).getYear());
        model.addAttribute("title", "Halls");
        return "hall";
    }

    @PostMapping("hall/edit")
    public @ResponseBody JsonResponse editHall(@RequestParam final long id,
                                               @Valid @ModelAttribute("hall") final HallForm hallForm,
                                               final BindingResult result) {
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
    public @ResponseBody JsonResponse addHall(@Valid @ModelAttribute("newHall") final HallForm hall,
                                              final BindingResult result, final Authentication authentication) {

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
    public String addYear(@Valid @ModelAttribute("newYear") final Year year, final BindingResult result,
                          final RedirectAttributes attributes, final Authentication authentication) {
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
        Set<User> admins = new HashSet<>(userRepository.findByRole(roleRepository.findByName("ROLE_ADMIN")));
        final Set<ApplicationForm> oldUsers = yearOld.getUsers();
        final Set<ApplicationForm> newUsers =
                oldUsers.stream().filter(it -> admins.contains(it.getUser())).map(it -> ApplicationForm.createNewUsers(it, year)).collect(Collectors.toSet());
        applicationFormRepository.save(newUsers);

        final Set<Hall> oldHalls = yearOld.getHalls();
        final Set<Hall> newHalls =
                oldHalls.stream().filter(it -> !it.isDef()).map(it -> Hall.createNewHall(it, year)).collect(Collectors.toSet());
        hallRepository.save(newHalls);

        final Set<PositionValue> oldPositions = yearOld.getPositionValues();
        final Set<PositionValue> newPositions =
                oldPositions.stream().filter(it -> !it.isDef()).map(it -> PositionValue.copyPosition(it, year)).collect(Collectors.toSet());
        positionValueRepository.save(newPositions);
        return "redirect:/admin/year/" + year.getId();
    }

    @PostMapping("/year/close")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public @ResponseBody JsonResponse closeYear(@RequestParam final long id, @RequestParam final boolean isOpen) {
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
        utils.setModelForAdmin(model, year);
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
    @PreAuthorize("hasRole('ADMIN')")
    public @ResponseBody JsonResponse<Day> saveEvent(@Valid @ModelAttribute final Day day, final BindingResult result
            , final Authentication authentication) {
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
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String addEvent(@Valid @ModelAttribute("newDay") final Day dayForm, final BindingResult result,
                           final RedirectAttributes attributes, final Authentication authentication) {
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
    @PreAuthorize("hasRole('ADMIN') or @dayService.isManagerForDay(authentication, #id)")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public String event(@PathVariable(value = "id") final long id, final Model model,
                        final Authentication authentication) {
        final User user = userService.getUserByAuthentication(authentication);

        final Day currentDay = dayRepository.findOne(id);
        final Year year = currentDay.getYear();
        utils.setModelForAdmin(model, year);

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
        return new HashMap<>(Arrays.stream(Attendance.values()).collect(Collectors.toMap(Function.identity(),
                attendance -> messageSource.getMessage("volunteers" + ".attendance." + attendance.name().toLowerCase(), null, attendance.name(), locale))));
    }

    @GetMapping("/day/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editEvent(@PathVariable(value = "id") final long id, final Model model,
                            final Authentication authentication) {
        final User user = userService.getUserByAuthentication(authentication);
        final Day day = dayRepository.findOne(id);
        final Year year = day.getYear();
        utils.setModelForAdmin(model, year);
        model.addAttribute("day", day);
        model.addAttribute("users", day.getUsers());
        final Map<UserDay, Map<Year, Set<Pair<Hall, PositionValue>>>> exp =
                day.getUsers().stream().collect(Collectors.toMap(Function.identity(),
                        u -> u.getUserYear().getUser().getApplicationForms().stream().filter(uy -> !uy.getYear().equals(user.getYear())).collect(Collectors.toMap(ApplicationForm::getYear, uy -> uy.getUserDays().stream().map(ud -> Pair.of(ud.getHall(), ud.getPosition())).collect(Collectors.toSet())))));
        model.addAttribute("exp", exp);
        return "day";
    }

    @PostMapping("/day/save")
    @PreAuthorize("hasRole('ADMIN')")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public @ResponseBody JsonResponse save(@RequestParam final long userId, @RequestParam final long hallId,
                                           @RequestParam final long positionId) {
        JsonResponse<Object> response = new JsonResponse<>();
        try {
            UserDay user = userEventRepository.findOne(userId);
            PositionValue positionValue = positionId == -1 ? user.getPosition() :
                    positionValueRepository.findOne(positionId);
            Hall hall = hallId == -1 ? user.getHall() : hallRepository.findOne(hallId);
            boolean isChanged = false;
            if (!user.getHall().equals(hall)) {
                user.setHall(hall);
                isChanged = true;
            }
            if (!user.getPosition().equals(positionValue)) {
                user.setPosition(positionValue);
                if (positionValue.getDefaultHall() != null) {
                    user.setHall(positionValue.getDefaultHall());
                }
                isChanged = true;
            }
            if (isChanged) {
                userEventRepository.save(user);
            }
            response.setStatus(Status.OK);
            final Map<String, Object> result = new HashMap<>();
            result.put("position", user.getPosition());
            result.put("hall", user.getHall());
            response.setResult(result);
        } catch (Exception e) {
            response.setResult(e.getMessage());
            response.setStatus(Status.FAIL);
        }
        return response;
    }

    @PostMapping("/day/copy")
    @PreAuthorize("hasRole('ADMIN')")
    public @ResponseBody JsonResponse copy(@RequestParam final long eventId, @RequestParam final long baseEventId,
                                           @RequestParam(required = false, name = "halls[]") final List<Long> halls) {
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
                    if (halls != null && !halls.isEmpty() && !halls.contains(userEventBase.get(form).getHall().getId())) {
                        continue;
                    }
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
    @PreAuthorize("hasRole('ADMIN') or @dayService.isManagerForDay(authentication, #id)")
    public String attendance(@PathVariable(value = "id") final long id, final Model model,
                             final Authentication authentication) {
        event(id, model, authentication);
        model.addAttribute("attendances", Attendance.values());
        model.addAttribute("attendance", true);
        return "showEvent";
    }

    @GetMapping("/day/{id}/assessments")
    @PreAuthorize("hasRole('ADMIN') or @dayService.isManagerForDay(authentication, #id)")
    public String assessments(@PathVariable(value = "id") final long id, final Model model,
                              final Authentication authentication) {
        event(id, model, authentication);
        final Day day = dayRepository.findOne(id);
        model.addAttribute("assessment", true);
        model.addAttribute("assessments",
                day.getUsers().stream().map(UserDay::getAssessments).flatMap(Collection::stream).collect(Collectors.toSet()));
        if (!model.containsAttribute("newAssessment")) {
            model.addAttribute("newAssessment", new Assessment());
        }
        return "showEvent";
    }

    @PostMapping("/day/assessments")
    @PreAuthorize("hasRole('ADMIN') or @dayService.isManagerForUserDay(authentication, #userId)")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public @ResponseBody JsonResponse setAssessments(@Valid @ModelAttribute("newAssessment") final Assessment assessment, final BindingResult result, @RequestParam final long userId, final Authentication authentication) {

        try {
            if (result.hasErrors()) {
                JsonResponse<List<ObjectError>> response = new JsonResponse<>();
                response.setStatus(Status.FAIL);
                response.setResult(result.getAllErrors());
                return response;
            }
            UserDay user = userEventRepository.findOne(userId);
            assessment.setUser(user);
            if (assessment.getAuthor() == null) {
                assessment.setAuthor(userService.getUserByAuthentication(authentication));
            }
            userEventAssessmentRepository.save(assessment);
            JsonResponse<AssessmentsJson> response = new JsonResponse<>();
            response.setStatus(Status.OK);

            response.setResult(new AssessmentsJson(assessment));
            return response;
        } catch (Exception e) {
            JsonResponse<String> response = new JsonResponse<>();
            response.setStatus(Status.FAIL);
            response.setResult(e.getMessage());
            return response;
        }
    }

    @PostMapping("/day/attendance")
    @PreAuthorize("hasRole('ADMIN') or @dayService.isManagerForUserDay(authentication, #id)")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public @ResponseBody JsonResponse setAttendance(@RequestParam final long id, @RequestParam final String value) {
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
        utils.setModelForAdmin(model, userService.getUserByAuthentication(authentication).getYear());
        model.addAttribute("medals", medalRepository.findAll());
        if (!model.containsAttribute("newMedal")) {
            model.addAttribute("newMedal", new MedalForm());
        }
        return "medals";
    }

    @PostMapping("/medals/add")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    public @ResponseBody JsonResponse addMedals(@Valid @ModelAttribute("newMedal") final MedalForm medalForm,
                                                final BindingResult result) {
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
    public @ResponseBody JsonResponse deleteMedal(@RequestParam("id") final long id) {
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
    public @ResponseBody JsonResponse updateMedal(@Valid @ModelAttribute("medal") final MedalForm medalForm,
                                                  final BindingResult result, @RequestParam("id") final long id) {
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

        utils.setModelForAdmin(model, userService.getUserByAuthentication(authentication).getYear());

        Pair<Map<ApplicationForm, Double>, Map<ApplicationForm, List<String>>> assessments =
                experienceService.getAssessments(year);
        Map<ApplicationForm, Double> experience = experienceService.getExperience(year);
        Map<ApplicationForm, Medal> medals = experienceService.getNewMedals(experience);
        List<ApplicationForm> applicationForms = experienceService.getSortedApplicationForms(assessments.getFirst(),
                medals);

        final Collection<AssBoundary> bounds = assBoundaryRepository.findByYear(year);

        model.addAttribute("applicationForms", applicationForms);
        model.addAttribute("assessments", assessments.getFirst());
        model.addAttribute("assessmentsGroupByDays", assessments.getSecond());
        model.addAttribute("experience", experience);
        model.addAttribute("medals", medals);
        model.addAttribute("halls", experienceService.getHalls(year));
        model.addAttribute("bounds",
                bounds.stream().map(it -> Double.toString(it.getValue())).collect(Collectors.joining(";")));

        return "results";
    }

    @GetMapping("/results/csv")
    public void getResults(final HttpServletResponse response, final Authentication authentication) throws IOException {
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"results.csv\"");
        try (PrintWriter writer = response.getWriter()) {
            writer.println("user,medal,experience");
            final Year year = userService.getUserByAuthentication(authentication).getYear();
            Pair<Map<ApplicationForm, Double>, Map<ApplicationForm, List<String>>> assessments =
                    experienceService.getAssessments(year);

            printResultIntoFile(year, it -> {
                if (assessments.getFirst().get(it.getLeft()) <= 0) {
                    return;
                }
                writer.print(it.getLeft().getUser().getBadgeName());
                writer.print(",");
                writer.print(it.getMiddle().getName());
                writer.print(",");
                writer.println(it.getRight());
            });


            final Map<ApplicationForm, Double> experience = experienceService.getExperience(year);
            final Map<ApplicationForm, Medal> medals = experienceService.getNewMedals(experience);
            final List<ApplicationForm> applicationForms =
                    experienceService.getSortedApplicationForms(assessments.getFirst(), medals);

            for (ApplicationForm user : applicationForms) {
                if (assessments.getFirst().get(user) <= 0.0) {
                    continue;
                }
                writer.print(user.getUser().getBadgeName());
                writer.print(",");
                writer.print(medals.get(user).getName());
                writer.print(",");
                writer.println(experience.get(user));
            }
        }
    }

    @GetMapping("/results/docx")
    public void getResultsAsCertificate(final HttpServletResponse response, final Authentication authentication,
                                        @RequestParam("place") final String place) throws Exception {
        final Year year = userService.getUserByAuthentication(authentication).getYear();
        Pair<Map<ApplicationForm, Double>, Map<ApplicationForm, List<String>>> assessments =
                experienceService.getAssessments(year);

        response.setContentType("application/vnd.openxmlformats-officedocument.wordprocessingml.document");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"certificates.docx\"");
        try (OutputStream stream = response.getOutputStream()) {
            final WordprocessingMLPackage templatePackage =
                    WordprocessingMLPackage.load(getClass().getClassLoader()
                                    .getResourceAsStream("templates/volunteer-certificate-template.docx")
                    );
            VariablePrepare.prepare(templatePackage);
            final MainDocumentPart mainTemplateDocumentPart = templatePackage.getMainDocumentPart();

            final Map<String, String> variables = new HashMap<>();
            variables.put("place", place);

            final AtomicReference<WordprocessingMLPackage> resultPackageReference = new AtomicReference<>();
            final AtomicReference<MainDocumentPart> resultMainDocumentPartReference = new AtomicReference<>();

            printResultIntoFile(year, it -> {
                if (assessments.getFirst().get(it.getLeft()) <= 0) {
                    return;
                }
                variables.put("volunteer", it.getLeft().getUser().getBadgeName());
                variables.put("award", it.getMiddle().getName());

                try {
                    Document page = (Document) XmlUtils.unwrap(
                                    XmlUtils.unmarshallFromTemplate(
                                            XmlUtils.marshaltoString(
                                                    mainTemplateDocumentPart.getJaxbElement(), true, false
                                            ), variables)
                    );
                    if (resultMainDocumentPartReference.get() == null) {
                        resultPackageReference.set(WordprocessingMLPackage.load(getClass().getClassLoader().getResourceAsStream("templates/volunteer-certificate-template.docx")));
                        resultMainDocumentPartReference.set(resultPackageReference.get().getMainDocumentPart());

                        resultMainDocumentPartReference.get().setJaxbElement(page);
                    } else {
                        final List<Object> pageContent = new ArrayList<>(page.getContent());
                        for (int i = 0; i < pageContent.size(); i++) {
                            resultMainDocumentPartReference.get().addObject(XmlUtils.deepCopy(pageContent.get(i)));
                        }
                    }
                } catch (final Exception e) {
                    e.printStackTrace();
                    throw new RuntimeException(e);
                }
            });

            if (resultPackageReference.get() != null) {
                resultPackageReference.get().save(stream);
            }
        }
    }

    private void printResultIntoFile(final Year year, Consumer<Triple<ApplicationForm, Medal, Double>> printer) {
        Pair<Map<ApplicationForm, Double>, Map<ApplicationForm, List<String>>> assessments =
                experienceService.getAssessments(year);
        final Map<ApplicationForm, Double> experience = experienceService.getExperience(year);
        final Map<ApplicationForm, Medal> medals = experienceService.getNewMedals(experience);
        final List<ApplicationForm> applicationForms =
                experienceService.getSortedApplicationForms(assessments.getFirst(), medals);

        for (ApplicationForm user : applicationForms) {
            printer.accept(Triple.of(user, medals.get(user), experience.get(user)));
        }
    }

    @GetMapping("/ifmo/export")
    public void exportIfmoVolunteers(final HttpServletResponse response, final Authentication authentication) throws IOException {
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"itmo-volunteers.csv\"");

        try (PrintWriter writer = response.getWriter()) {

            writer.println("name,isu_number,group");
            final Year year = userService.getUserByAuthentication(authentication).getYear();
            Pair<Map<ApplicationForm, Double>, Map<ApplicationForm, List<String>>> assessments =
                    experienceService.getAssessments(year);

            for (Map.Entry<ApplicationForm, Double> entry : assessments.getFirst().entrySet()) {
                if (entry.getValue() <= 0.0) {
                    continue;
                }
                if (StringUtils.isNotBlank(entry.getKey().getUser().getItmoId())) {
                    writer.print(entry.getKey().getUser().getBadgeNameCyr());
                    writer.print(",");
                    writer.print(entry.getKey().getUser().getItmoId());
                    writer.print(",");
                    writer.println(entry.getKey().getGroup());
                }
            }
        }
    }

    @PostMapping("/results/bounds")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @ResponseBody
    public JsonResponse updateBounds(@RequestParam("bounds") final String bounds, final Authentication authentication) {
        final Year year = userService.getUserByAuthentication(authentication).getYear();
        assBoundaryRepository.delete(assBoundaryRepository.findByYear(year));

        final Collection<AssBoundary> parsedBounds =
                Arrays.stream(bounds.split(";")).map(StringUtils::deleteWhitespace).filter(this::isNumber).map(value -> new AssBoundary(Double.parseDouble(value), year)).collect(Collectors.toList());

        assBoundaryRepository.save(parsedBounds);

        final JsonResponse<String> result = new JsonResponse<>();
        result.setStatus(Status.OK);
        result.setResult(parsedBounds.stream().map(it -> Double.toString(it.getValue())).collect(Collectors.joining(
                ";")));

        return result;
    }

    private boolean isNumber(final String value) {
        try {
            Double.parseDouble(value);
            return true;
        } catch (final NumberFormatException e) {
            return false;
        }
    }

    @PostMapping("/results")
    @Transactional(propagation = Propagation.REQUIRED, rollbackFor = Exception.class)
    @ResponseBody
    public JsonResponse addExp(@RequestParam("id") final long id, @RequestParam("exp") final double exp,
                               final Authentication authentication) {
        final Year year = userService.getUserByAuthentication(authentication).getYear();
        ApplicationForm user = applicationFormRepository.findOne(id);
        user.setExtraExperience(exp);
        final double newExp = experienceService.getExperience(year, user);

        JsonResponse<Double> result = new JsonResponse<>();
        result.setStatus(Status.OK);
        result.setResult(newExp);
        return result;
    }


    @GetMapping("/results/user/{id}")
    public String detailedResultUser(@PathVariable final long id, final Model model,
                                     final Authentication authentication) {
        ApplicationForm applicationForm = applicationFormRepository.findOne(id);
        Map<Attendance, String> attendanceComments = getAttendanceMap();
        Map<Long, Pair<Double, String>> attendance =
                applicationForm.getUserDays().stream().collect(Collectors.toMap(UserDay::getId,
                        d -> Pair.of(d.calcAttendanceScore(), attendanceComments.get(d.getAttendance()))));

        utils.setModelForAdmin(model, userService.getUserByAuthentication(authentication).getYear());
        model.addAttribute("assessmentattendance", attendance);
        model.addAttribute("assessmentuser", applicationForm);
        return "detailedResult";
    }

    @GetMapping("/events")
    public String getEvents(final Model model, final Authentication authentication, final HttpServletRequest request) throws IOException {
        User user = userService.getUserByAuthentication(authentication);
        Year year = user.getYear();
        utils.setModelForAdmin(model, year);
        String file = year.getCalendar();
        model.addAttribute("file", file);
        URL url = new URL(request.getRequestURL().toString());
        String baseUrl = url.getProtocol() + "://" + url.getHost() + (url.getPort() == 80 || url.getPort() < 0 ? "" :
                ":" + url.getPort());
        model.addAttribute("baseUrl", baseUrl);
        return "events";
    }

    @PostMapping("/events")
    public @ResponseBody JsonResponse editEvents(@RequestParam("file") final String file,
                                                 Authentication authentication) {
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
        utils.setModelForAdmin(model, user.getYear());
        model.addAttribute("mailform", new MailForm());
        return "email";
    }

    @PostMapping("/email")
    public String sendEmail(@Valid @ModelAttribute("mailform") final MailForm mailForm, final BindingResult result,
                            Authentication authentication, final Model model) throws MessagingException {
        User user = userService.getUserByAuthentication(authentication);
        if (result.hasErrors()) {
            utils.setModelForAdmin(model, user.getYear());
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
        utils.setModelForAdmin(model, user.getYear());
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
        utils.setModelForAdmin(model, user.getYear());
        model.addAttribute("users", userRepository.findAll());
        return "users";
    }

    @GetMapping("/users/{id}")
    public String getUser(@PathVariable final long id, final Model model, final Authentication authentication) {
        utils.setModelForAdmin(model, userService.getUserByAuthentication(authentication).getYear());

        if (!model.containsAttribute("editForm")) {
            User user = userRepository.findOne(id);
            model.addAttribute("editForm", new UserEditForm(user));
        }

        return "userEdit";
    }

    @PutMapping("/users/{id}")
    public String editUser(@PathVariable final long id, @Valid @ModelAttribute final UserEditForm editForm,
                           final BindingResult result, final RedirectAttributes attributes, final Model model,
                           final Authentication authentication) {
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
    @PreAuthorize("hasRole('ADMIN')")
    public void getBadges(final HttpServletResponse response, @PathVariable("id") long id) throws IOException {
        response.setContentType("text/csv");
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Content-Disposition", "attachment; filename=\"file.csv\"");
        try (PrintWriter writer = response.getWriter()) {
            writer.write("Team,TeamCur,Role,RoleCur,Name,NameCyr,Medal,Stars\n");
            Day day = dayRepository.findOne(id);
            Map<ApplicationForm, Double> exp = experienceService.getExperienceExceptCurrentYear(day.getYear());
            Map<ApplicationForm, Medal> medals = experienceService.getNewMedals(exp);
            day.getUsers().forEach(u -> {
                User user = u.getUserYear().getUser();
                String stars = new String(new char[(int) medals.get(u.getUserYear()).getStars()]).replace('\0', '★');
                writer.write(u.getHall().getName() + "," + u.getHall().getCurName() + "," + u.getPosition().getEngName() + "," + u.getPosition().getCurName() + ",\"" + user.getFirstName() + "\n" + user.getLastName() + "\",\"" + user.getFirstNameCyr() + "\n" + user.getLastNameCyr() + "\"," + medals.get(u.getUserYear()).getName() + "," + stars + "\n");
            });
            writer.flush();
        }
    }

    @GetMapping("/stars")
    public String stars(Model model, Authentication authentication) {
        Year year = userService.getUserByAuthentication(authentication).getYear();
        utils.setModelForAdmin(model, userService.getUserByAuthentication(authentication).getYear());
        model.addAttribute("medals", experienceService.getMedals(year));
        model.addAttribute("exp", experienceService.getExperienceExceptCurrentYear(year));
        return "star";
    }

    @PostMapping("/default")
    public @ResponseBody JsonResponse<String> setDefaultYear(Authentication authentication) {
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

package ru.ifmo.neerc.volunteers.controller;

import java.util.Optional;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import ru.ifmo.neerc.volunteers.dto.UserDayDto;
import ru.ifmo.neerc.volunteers.entity.Day;
import ru.ifmo.neerc.volunteers.entity.Hall;
import ru.ifmo.neerc.volunteers.entity.User;
import ru.ifmo.neerc.volunteers.entity.UserDay;
import ru.ifmo.neerc.volunteers.entity.Year;
import ru.ifmo.neerc.volunteers.repository.DayRepository;
import ru.ifmo.neerc.volunteers.repository.UserEventRepository;
import ru.ifmo.neerc.volunteers.repository.UserRepository;
import ru.ifmo.neerc.volunteers.repository.YearRepository;

@AllArgsConstructor
@RestController
@RequestMapping("/api")
@Slf4j
public class ApiController {
    private final PasswordEncoder passwordEncoder;

    private final DayRepository dayRepository;
    private final UserEventRepository userDayRepository;
    private final UserRepository userRepository;
    private final YearRepository yearRepository;

    @PostMapping("/auth")
    public ResponseEntity<Void> auth(@RequestParam final String username, @RequestParam final String password) {
        User user = userRepository.findByEmailIgnoreCase(username);
        if (user != null && passwordEncoder.matches(password, user.getPassword()) && user.isChatLoginAllowed()) {
            log.debug("Authenticated user {}", username);
            return ResponseEntity.ok(null);
        }

        log.warn("Failed to authenticate user {}", username);
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/halls")
    public ResponseEntity<Set<Hall>> getHallsForYear(@RequestParam("year") final long yearId) {
        final Year year = yearRepository.findOne(yearId);
        if (year != null) {
            return ResponseEntity.ok(year.getHalls());
        }

        return ResponseEntity.notFound().build();
    }

    @GetMapping("/user")
    public ResponseEntity<UserDayDto> getUserDay(@RequestParam final String username, @RequestParam("day") final long dayId) {
        final User user = userRepository.findByEmailIgnoreCase(username);

        if (user == null) {
            log.warn("User {} not found", username);
            return ResponseEntity.notFound().build();
        }

        if (user.getChatAlias() != null && !user.getChatAlias().isEmpty()) {
            return ResponseEntity.ok(new UserDayDto(user));
        }

        final Day day = dayRepository.findOne(dayId);
        final Optional<UserDay> userDay = userDayRepository.findByDay(day)
                                                           .stream()
                                                           .filter(u -> u.getUserYear().getUser().equals(user))
                                                           .findFirst();
        if (userDay.isPresent()) {
            return ResponseEntity.ok(new UserDayDto(userDay.get()));
        }

        log.warn("User {} is not registered for day {}", username, dayId);
        return ResponseEntity.notFound().build();
    }
}

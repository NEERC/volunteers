package ru.ifmo.neerc.volunteers.service;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.ui.Model;
import ru.ifmo.neerc.volunteers.entity.Day;
import ru.ifmo.neerc.volunteers.entity.Role;
import ru.ifmo.neerc.volunteers.entity.User;
import ru.ifmo.neerc.volunteers.entity.Year;
import ru.ifmo.neerc.volunteers.form.HallForm;
import ru.ifmo.neerc.volunteers.form.PositionForm;
import ru.ifmo.neerc.volunteers.repository.RoleRepository;
import ru.ifmo.neerc.volunteers.repository.YearRepository;
import ru.ifmo.neerc.volunteers.service.year.YearService;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;

/**
 * Created by Lapenok Akesej on 03.09.2017.
 */
@Service
@AllArgsConstructor
public class Utils {

    final YearRepository yearRepository;
    final RoleRepository roleRepository;
    final YearService yearService;

    public void setModelForUser(Model model, User user) {
        setModel(model, user);
        model.addAttribute("isUser", true);
    }


    private void setModel(Model model, User user) {
        Year year = yearService.getYear(user);
        model.addAttribute("year", year);
        model.addAttribute("years", yearRepository.findAll());

        if (year != null) {
            model.addAttribute("days", year.getDays());
            model.addAttribute("positions", year.getPositionValues());
            model.addAttribute("halls", year.getHalls());
        } else {
            model.addAttribute("days", Collections.EMPTY_LIST);
            model.addAttribute("positions", Collections.EMPTY_LIST);
            model.addAttribute("halls", Collections.EMPTY_LIST);
        }
    }

    public void setModelForAdmin(Model model, User user) {
        setModel(model, user);
        if (!model.containsAttribute("newYear")) {
            model.addAttribute("newYear", new Year());
        }
        if (!model.containsAttribute("newDay")) {
            final Day newDay = new Day();
            newDay.setYear(yearService.getYear(user));
            model.addAttribute("newDay", newDay);
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

    public String getAppUrl(HttpServletRequest request) {
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath();
    }
}

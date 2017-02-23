package com.volunteer.home.service;

/**
 * Created by Алексей on 23.02.2017.
 */
public interface SecurityService {
    String findLoggedInUsername();

    void autologin(String username, String password);
}

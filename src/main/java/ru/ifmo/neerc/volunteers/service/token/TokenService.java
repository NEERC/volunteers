package ru.ifmo.neerc.volunteers.service.token;

public interface TokenService {
    String getToken();
    void revokeToken(String token);
}

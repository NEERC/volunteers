package ru.ifmo.neerc.volunteers.service.token;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import lombok.AllArgsConstructor;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;
import org.springframework.security.oauth2.provider.token.AuthorizationServerTokenServices;
import org.springframework.security.oauth2.provider.token.ConsumerTokenServices;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class TokenServiceImpl implements TokenService {
    private static final String CLIENT_ID = "volunteers";

    private final AuthorizationServerTokenServices tokenServices;
    private final ConsumerTokenServices consumerTokenServices;

    @Override
    public String getToken() {
        Map<String, String> requestParameters = new HashMap<>();
        Set<GrantedAuthority> authorities = new HashSet<>();
        Set<String> scopes = new HashSet<>();
        Set<String> resourceIds = new HashSet<>();
        Set<String> responseTypes = new HashSet<>();
        Map<String, Serializable> extensionProperties = new HashMap<>();

        OAuth2Request request = new OAuth2Request(requestParameters,
                                                  CLIENT_ID,
                                                  authorities,
                                                  true,
                                                  scopes,
                                                  resourceIds,
                                                  "",
                                                  responseTypes,
                                                  extensionProperties);

        OAuth2Authentication authentication = new OAuth2Authentication(request, null);

        OAuth2AccessToken token = tokenServices.createAccessToken(authentication);
        return token.getValue();
    }

    @Override
    public void revokeToken(String token) {
        consumerTokenServices.revokeToken(token);
    }
}

package com.campusrecycle.security;

import com.campusrecycle.config.AppProperties;
import com.campusrecycle.model.User;
import com.campusrecycle.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Component
public class OAuth2AuthenticationSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final UserService userService;
    private final AppProperties appProperties;

    public OAuth2AuthenticationSuccessHandler(JwtTokenProvider jwtTokenProvider,
                                              UserService userService,
                                              AppProperties appProperties) {
        this.jwtTokenProvider = jwtTokenProvider;
        this.userService = userService;
        this.appProperties = appProperties;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication) throws IOException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        String githubId = String.valueOf(oAuth2User.getAttribute("id"));
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");
        String avatarUrl = oAuth2User.getAttribute("avatar_url");

        if (name == null) name = oAuth2User.getAttribute("login");
        if (email == null) email = githubId + "@github.user";

        User user = userService.findOrCreateUser(githubId, email, name, avatarUrl);
        String token = jwtTokenProvider.generateToken(
                String.valueOf(user.getId()),
                user.getEmail(),
                user.getName()
        );

        String redirectUrl = UriComponentsBuilder
                .fromUriString(appProperties.getFrontendUrl())
                .path("/auth/callback")
                .queryParam("token", token)
                .build()
                .toUriString();

        getRedirectStrategy().sendRedirect(request, response, redirectUrl);
    }
}

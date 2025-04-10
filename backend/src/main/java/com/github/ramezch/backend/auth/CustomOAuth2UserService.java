package com.github.ramezch.backend.auth;

import com.github.ramezch.backend.appuser.AppUser;
import com.github.ramezch.backend.appuser.AppUserRepository;
import com.github.ramezch.backend.appuser.AppUserRoles;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final AppUserRepository appUserRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);
        String provider = userRequest.getClientRegistration().getRegistrationId();

        AppUser appUser = appUserRepository.findById(oAuth2User.getName())
                .orElseGet(() -> createUser(oAuth2User, provider));

        appUser.setAttributes(oAuth2User.getAttributes());
        appUser.setSimpleGrantedAuthorities(List.of(new SimpleGrantedAuthority("ROLE_" + appUser.getRole())));

        return appUser;
    }

    private AppUser createUser(OAuth2User oAuth2User, String provider) {
        String id;
        String username;
        String avatarUrl;

        if ("github".equals(provider)) {
            id = String.valueOf(Objects.requireNonNull(oAuth2User.getAttribute("id")));
            username = oAuth2User.getAttribute("login");
            avatarUrl = oAuth2User.getAttribute("avatar_url");
        } else if ("google".equals(provider)) {
            id = oAuth2User.getAttribute("sub");
            username = oAuth2User.getAttribute("email");
            avatarUrl = oAuth2User.getAttribute("picture");
        } else {
            throw new IllegalArgumentException("Unsupported provider: " + provider);
        }

        AppUser appUser = AppUser.builder()
                .id(id)
                .username(username)
                .avatarUrl(avatarUrl)
                .customerIds(new ArrayList<>())
                .internetPlanIds(new ArrayList<>())
                .role(AppUserRoles.USER)
                .attributes(oAuth2User.getAttributes())
                .build();

        return appUserRepository.save(appUser);
    }

}
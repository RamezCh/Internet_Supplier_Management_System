package com.github.ramezch.backend.appuser;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Transient;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AppUser implements OAuth2User {
    private String id;
    private String username;
    private String avatarUrl;
    private List<String> customerIds;
    private AppUserRoles role;

    @Transient
    @JsonIgnore
    private Map<String, Object> attributes;

    @Transient
    @JsonIgnore
    private List<SimpleGrantedAuthority> simpleGrantedAuthorities;

    @Override
    @JsonIgnore
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return simpleGrantedAuthorities;
    }

    @Override
    @JsonIgnore
    public String getName() {
        return id;
    }

    @JsonIgnore
    public List<String> getCustomerIds() {
        return customerIds;
    }

    @JsonIgnore
    public void setCustomerIds(List<String> newCustomerIds) {
        this.customerIds = newCustomerIds;
    }
}

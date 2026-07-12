package zm.unza.tels.cemis.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import zm.unza.tels.cemis.entity.User;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

public class CemisUserDetails implements UserDetails {

    private final UUID   id;
    private final String email;
    private final String passwordHash;
    private final String role;
    private final boolean active;

    public CemisUserDetails(User u) {
        this.id           = u.getId();
        this.email        = u.getEmail();
        this.passwordHash = u.getPasswordHash();
        this.role         = u.getRole().name();
        this.active       = u.isActive();
    }

    public UUID   getId()   { return id; }
    public String getRole() { return role; }

    @Override public String getUsername()  { return email; }
    @Override public String getPassword()  { return passwordHash; }
    @Override public boolean isEnabled()   { return active; }
    @Override public boolean isAccountNonExpired()   { return true; }
    @Override public boolean isAccountNonLocked()    { return true; }
    @Override public boolean isCredentialsNonExpired() { return true; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase()));
    }
}

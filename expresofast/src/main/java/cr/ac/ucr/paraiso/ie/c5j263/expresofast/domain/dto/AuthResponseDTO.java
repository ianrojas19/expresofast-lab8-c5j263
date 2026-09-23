package cr.ac.ucr.paraiso.ie.c5j263.expresofast.domain.dto;

import java.util.List;

public class AuthResponseDTO {
    private String token;
    private String username;
    private List<String> roles;
    private long expirationTime;

    public AuthResponseDTO(String token, String username, List<String> roles, long expirationTime) {
        this.token = token;
        this.username = username;
        this.roles = roles;
        this.expirationTime = expirationTime;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }

    public long getExpirationTime() { return expirationTime; }
    public void setExpirationTime(long expirationTime) { this.expirationTime = expirationTime; }
}

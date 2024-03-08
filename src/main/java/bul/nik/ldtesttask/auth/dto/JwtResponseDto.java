package bul.nik.ldtesttask.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@AllArgsConstructor()
public class JwtResponseDto {
    private String accessToken;
    private String refreshToken;
    private String username;
    private String email;
    private Set<String> roles;
}

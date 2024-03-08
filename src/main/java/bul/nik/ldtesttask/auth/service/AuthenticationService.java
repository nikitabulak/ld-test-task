package bul.nik.ldtesttask.auth.service;

import bul.nik.ldtesttask.auth.configuration.jwt.JwtService;
import bul.nik.ldtesttask.auth.dto.JwtResponseDto;
import bul.nik.ldtesttask.auth.dto.LoginRequestDto;
import bul.nik.ldtesttask.exception.UserNotFoundException;
import bul.nik.ldtesttask.token.Token;
import bul.nik.ldtesttask.token.TokenRepository;
import bul.nik.ldtesttask.token.TokenService;
import bul.nik.ldtesttask.user.model.User;
import bul.nik.ldtesttask.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final UserDetailsService userService;
    private final TokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;


    public ResponseEntity<JwtResponseDto> authenticate(LoginRequestDto loginRequestDto) {
        User requestedUser = (User) userService.loadUserByUsername(loginRequestDto.getUsernameOrEmail());
        Authentication auth = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(
                        loginRequestDto.getUsernameOrEmail(),
                        loginRequestDto.getPassword()));

        SecurityContextHolder.getContext().setAuthentication(auth);
        return generateResponseWithTokens(requestedUser, loginRequestDto.isRememberMe());
    }

    private ResponseEntity<JwtResponseDto> generateResponseWithTokens(User user, boolean rememberMe) {
        String accessToken = jwtService.generateAccessToken(user);
        String refreshToken = jwtService.generateRefreshToken(user, rememberMe);
        Optional<Token> token = tokenRepository.findTokenByUserId(user.getId());
        if (token.isPresent()) {
            tokenService.updateRefreshToken(user, passwordEncoder.encode(refreshToken));
        } else {
            tokenService.saveRefreshToken(user, passwordEncoder.encode(refreshToken), rememberMe);
        }

        return ResponseEntity.ok(new JwtResponseDto(accessToken,
                refreshToken,
                user.getUsername(),
                user.getEmail(),
                user.getRoles().stream()
                        .map(r -> r.getName().name().split("_")[1].toLowerCase())
                        .collect(Collectors.toSet())));
    }

    public void refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtService.parseJwtFromRequest(request);

        String username = jwtService.getUserNameFromJwt(refreshToken);
        User user = userRepository.findUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
        Optional<Token> token = tokenRepository.findTokenByUserId(user.getId());

        if (token.isPresent() && token.get().isValid() &&
                passwordEncoder.matches(refreshToken, token.get().getRefreshToken()) &&
                jwtService.validateRefreshToken(refreshToken)) {
            String accessToken = jwtService.generateAccessToken(user);
            refreshToken = jwtService.generateRefreshToken(user, jwtService.getRememberMeFromJwt(refreshToken));

            tokenService.updateRefreshToken(user, passwordEncoder.encode(refreshToken));

            try {
                response.setHeader("content-type", "application/json");
                new ObjectMapper().writeValue(response.getOutputStream(), new JwtResponseDto(accessToken,
                        refreshToken,
                        user.getUsername(),
                        user.getEmail(),
                        user.getRoles().stream()
                                .map(r -> r.getName().name().split("_")[1].toLowerCase())
                                .collect(Collectors.toSet())));
            } catch (IOException e) {
                log.warn("Error while writing response with new tokens", e);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

}

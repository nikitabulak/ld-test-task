package bul.nik.ldtesttask.auth.configuration;

import bul.nik.ldtesttask.auth.configuration.jwt.JwtService;
import bul.nik.ldtesttask.exception.UserNotFoundException;
import bul.nik.ldtesttask.token.TokenService;
import bul.nik.ldtesttask.user.model.User;
import bul.nik.ldtesttask.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static bul.nik.ldtesttask.auth.configuration.jwt.JwtExceptionFilter.writeExceptionResponse;

@Service
@Slf4j
@RequiredArgsConstructor
public class LogoutService implements LogoutHandler {
    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final JwtService jwtService;

    @Override
    public void logout(HttpServletRequest request, HttpServletResponse response,
                       Authentication authentication) {
        final String accessToken = jwtService.parseJwtFromRequest(request);
        if (accessToken != null) {
            try {
                String username = jwtService.getUserNameFromJwt(accessToken);
                User user = userRepository.findUserByUsername(username).orElseThrow(() ->
                        new UserNotFoundException(username));
                tokenService.revokeRefreshToken(user);
                SecurityContextHolder.clearContext();
            } catch (RuntimeException ex) {
                writeExceptionResponse(request, response, ex, "Logout error");
            }
        }
    }
}

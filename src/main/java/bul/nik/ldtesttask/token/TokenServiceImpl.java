package bul.nik.ldtesttask.token;

import bul.nik.ldtesttask.exception.UserNotFoundException;
import bul.nik.ldtesttask.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TokenServiceImpl implements TokenService {
    private final TokenRepository tokenRepository;

    @Override
    @Transactional
    public void saveRefreshToken(User user, String jwtToken, boolean rememberMe) {
        Token token = Token.builder()
                .id(UUID.randomUUID())
                .user(user)
                .refreshToken(jwtToken)
                .tokenType(TokenType.BEARER)
                .tokenPurpose(TokenPurpose.REFRESH)
                .rememberMe(rememberMe)
                .expired(false)
                .revoked(false)
                .build();
        tokenRepository.save(token);
    }

    @Override
    @Transactional
    public void updateRefreshToken(User user, String jwtToken) {
        Token token = tokenRepository.findTokenByUserId(user.getId())
                .orElseThrow(() -> new UserNotFoundException(user.getUsername()));
        token.setRefreshToken(jwtToken);
        token.setRevoked(false);
        token.setExpired(false);
        tokenRepository.save(token);
    }


    @Override
    @Transactional
    public void revokeRefreshToken(User user) {
        Optional<Token> userToken = tokenRepository.findTokenByUserId(user.getId());
        if (userToken.isPresent()) {
            Token token = userToken.get();
            token.setExpired(true);
            token.setRevoked(true);
            tokenRepository.save(token);
        }
    }
}

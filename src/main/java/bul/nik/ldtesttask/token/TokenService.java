package bul.nik.ldtesttask.token;

import bul.nik.ldtesttask.user.model.User;

public interface TokenService {
    void saveRefreshToken(User user, String jwtToken, boolean rememberMe);

    void updateRefreshToken(User user, String jwtToken);

    void revokeRefreshToken(User user);
}

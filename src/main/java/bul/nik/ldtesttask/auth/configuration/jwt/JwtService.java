package bul.nik.ldtesttask.auth.configuration.jwt;


import bul.nik.ldtesttask.token.TokenPurpose;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final Key key = Keys.secretKeyFor(SignatureAlgorithm.HS512);
    @Value("${app.accessTokenExpirationMs}")
    private long accessTokenExpirationMs;
    @Value("${app.refreshTokenExpirationMs}")
    private long refreshTokenExpirationMs;
    @Value("${app.refreshTokenRememberMeExpirationMs}")
    private long refreshTokenRememberMeExpirationMs;

    public String generateAccessToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", TokenPurpose.ACCESS);
        return generateToken(userDetails, claims, accessTokenExpirationMs);
    }

    public String generateRefreshToken(UserDetails userDetails, boolean rememberMe) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("type", TokenPurpose.REFRESH);
        claims.put("rememberMe", rememberMe);
        return generateToken(userDetails, claims, rememberMe ? refreshTokenRememberMeExpirationMs : refreshTokenExpirationMs);
    }

    private String generateToken(UserDetails userDetails, Map<String, Object> claims, long expirationTime) {
        return Jwts.builder().setSubject((userDetails.getUsername())).setIssuedAt(new Date())
                .setExpiration(new Date((new Date()).getTime() + expirationTime))
                .addClaims(claims)
                .signWith(key, SignatureAlgorithm.HS512).compact();
    }

    public boolean validateAccessToken(String jwt) {
        return Jwts.parser().setSigningKey(key)
                .parseClaimsJws(jwt)
                .getBody()
                .getExpiration()
                .after(new Date()) &&
                getPurposeFromJwt(jwt).equals(TokenPurpose.ACCESS.name());
    }

    public boolean validateRefreshToken(String jwt) {
        try {
            return Jwts.parser().setSigningKey(key)
                    .parseClaimsJws(jwt)
                    .getBody()
                    .getExpiration()
                    .after(new Date())
                    &&
                    getPurposeFromJwt(jwt).equals(TokenPurpose.REFRESH.name());
        } catch (MalformedJwtException | IllegalArgumentException e) {
            log.warn("Exception while validating refresh token", e);
        }
        return false;
    }


    public String parseJwtFromRequest(HttpServletRequest request) {
        String headerAuth = request.getHeader(HttpHeaders.AUTHORIZATION);

        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer ")) {
            return headerAuth.substring(7);
        }
        return null;
    }

    public String getUserNameFromJwt(String jwt) {
        return Jwts.parser().setSigningKey(key).parseClaimsJws(jwt).getBody().getSubject();
    }

    public String getPurposeFromJwt(String jwt) {
        return (String) Jwts.parser().setSigningKey(key).parseClaimsJws(jwt).getBody().get("type");
    }

    public boolean getRememberMeFromJwt(String jwt) {
        return (boolean) Jwts.parser().setSigningKey(key).parseClaimsJws(jwt).getBody().get("rememberMe");
    }

}

package bul.nik.ldtesttask.auth.configuration.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
public class JwtExceptionFilter extends OncePerRequestFilter {
    public static void writeExceptionResponse(HttpServletRequest request, HttpServletResponse response, Exception ex, String errorMessage) {
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        final Map<String, Object> body = new HashMap<>();
        body.put("status", HttpServletResponse.SC_UNAUTHORIZED);
        body.put("error", errorMessage);
        body.put("message", ex.getMessage());
        body.put("path", request.getServletPath());
        response.addHeader("WWW-Authenticate", "Bearer realm=\"LD\"");

        final ObjectMapper mapper = new ObjectMapper();
        try {
            mapper.writeValue(response.getOutputStream(), body);
        } catch (IOException e) {
            log.warn("Error while writing exception problem status", e);
        }
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response, FilterChain filterchain) throws ServletException, IOException {
        try {
            filterchain.doFilter(request, response);
        } catch (JwtException ex) {
            writeExceptionResponse(request, response, ex, ex instanceof ExpiredJwtException ? "JWT expired" : "JWT problem");
        }
    }
}

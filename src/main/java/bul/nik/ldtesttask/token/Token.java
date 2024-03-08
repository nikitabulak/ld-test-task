package bul.nik.ldtesttask.token;


import bul.nik.ldtesttask.user.model.User;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

import javax.persistence.*;
import java.util.UUID;

@Setter
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "tokens")
public class Token {

    @Id
    @UuidGenerator
    private UUID id;

    @Column(unique = true)
    private String refreshToken;

    @Column(name = "token_type")
    @Enumerated(EnumType.STRING)
    private TokenType tokenType = TokenType.BEARER;

    @Column(name = "token_purpose")
    @Enumerated(EnumType.STRING)
    private TokenPurpose tokenPurpose;
    @Column(name = "remember_me")
    private boolean rememberMe;
    private boolean revoked;

    private boolean expired;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;

    public boolean isValid() {
        return !revoked && !expired;
    }
}

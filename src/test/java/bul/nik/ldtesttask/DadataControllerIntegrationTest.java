package bul.nik.ldtesttask;

import bul.nik.ldtesttask.user.model.User;
import bul.nik.ldtesttask.user.repository.UserRepository;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class DadataControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private UserRepository userRepository;

    @Test
    void adminCanAuthenticate() throws Exception {
        UserDetails userDetails = userRepository.findUserByUsername("admin").orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        User userBeforeNumberCheck = userRepository.findUserByUsername("casualuser").orElseThrow();
        assertThat(userBeforeNumberCheck.getPhoneNumberData()).isNull();

        mockMvc.perform(
                        post("/dadata/check_phone_number/" + userBeforeNumberCheck.getId())
                                .secure(true)
                )
                .andExpect(status().isOk());

        User userAfterNumberCheck = userRepository.findUserByUsername("casualuser").orElseThrow();
        assertThat(userAfterNumberCheck.getPhoneNumberData()).isNotNull();
    }

}

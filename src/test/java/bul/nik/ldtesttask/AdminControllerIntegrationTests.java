package bul.nik.ldtesttask;

import bul.nik.ldtesttask.auth.dto.JwtResponseDto;
import bul.nik.ldtesttask.auth.dto.LoginRequestDto;
import bul.nik.ldtesttask.report.model.Report;
import bul.nik.ldtesttask.report.model.ReportStatus;
import bul.nik.ldtesttask.report.repository.ReportRepository;
import bul.nik.ldtesttask.user.model.ERole;
import bul.nik.ldtesttask.user.model.Role;
import bul.nik.ldtesttask.user.model.User;
import bul.nik.ldtesttask.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AdminControllerIntegrationTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReportRepository reportRepository;

    @Test
    @Order(1)
    void adminCanAuthenticate() throws Exception {
        LoginRequestDto operatorLoginRequest = new LoginRequestDto();
        operatorLoginRequest.setUsernameOrEmail("admin");
        operatorLoginRequest.setPassword("adminpassword");
        operatorLoginRequest.setRememberMe(false);

        MockHttpServletResponse response = mockMvc.perform(
                        post("/auth/login")
                                .content(objectMapper.writeValueAsString(operatorLoginRequest))
                                .contentType(MediaType.APPLICATION_JSON).secure(true))
                .andReturn().getResponse();

        assertThat(objectMapper.readValue(response.getContentAsString(), JwtResponseDto.class)).isNotNull();
        assertThat(objectMapper.readValue(response.getContentAsString(), JwtResponseDto.class))
                .hasFieldOrProperty("accessToken").isNotNull();
        assertThat(objectMapper.readValue(response.getContentAsString(), JwtResponseDto.class))
                .hasFieldOrProperty("refreshToken").isNotNull();
        assertThat(response.getStatus()).isEqualTo(200);
    }

    @Test
    @Order(2)
    void adminCanGetAllUsersList() throws Exception {
        UserDetails userDetails = userRepository.findUserByUsername("admin").orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        mockMvc.perform(
                        get("/admin/users")
                                .secure(true)
                                .param("pageNumber", "0")
                )
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("content").isNotEmpty())
                .andExpect(jsonPath("totalElements").value("4"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(3)
    void adminCanGiveOperatorRightsToUser() throws Exception {
        UserDetails userDetails = userRepository.findUserByUsername("admin").orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        User futureOperator = userRepository.findUserByUsername("futureoperator").orElseThrow();
        assertThat(userRepository.findUserById(futureOperator.getId()).orElseThrow()
                .getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toList()))
                .doesNotContain(ERole.ROLE_OPERATOR);
        mockMvc.perform(
                        patch("/admin/users/" + futureOperator.getId())
                                .secure(true)
                )
                .andExpect(status().isOk());
        assertThat(userRepository.findUserById(futureOperator.getId()).orElseThrow()
                .getRoles()
                .stream()
                .map(Role::getName)
                .collect(Collectors.toList()))
                .contains(ERole.ROLE_OPERATOR);
    }

    @Test
    @Order(4)
    void adminCanGetReportsWithAllStatusesExceptDraftStatus() throws Exception {
        UserDetails operatorUserDetails = userRepository.findUserByUsername("admin").orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(operatorUserDetails, null, operatorUserDetails.getAuthorities()));

        Report sentReport = new Report(UUID.randomUUID(),
                userRepository.findUserByUsername("casualuser").orElseThrow(),
                ReportStatus.SENT,
                "Sent report content",
                LocalDateTime.now(),
                null,
                null);
        Report draftReport = new Report(UUID.randomUUID(),
                userRepository.findUserByUsername("casualuser").orElseThrow(),
                ReportStatus.DRAFT,
                "Draft report content",
                LocalDateTime.now(),
                null,
                null);
        Report acceptedReport = new Report(UUID.randomUUID(),
                userRepository.findUserByUsername("casualuser").orElseThrow(),
                ReportStatus.ACCEPTED,
                "Accepted report content",
                LocalDateTime.now(),
                null,
                null);
        Report rejectedReport = new Report(UUID.randomUUID(),
                userRepository.findUserByUsername("casualuser").orElseThrow(),
                ReportStatus.REJECTED,
                "Rejected report content",
                LocalDateTime.now(),
                null,
                null);
        reportRepository.save(sentReport);
        reportRepository.save(draftReport);
        reportRepository.save(acceptedReport);
        reportRepository.save(rejectedReport);

        mockMvc.perform(
                        get("/admin/reports")
                                .secure(true)
                                .param("sort", "asc")
                                .param("pageNumber", "0")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("content").isNotEmpty())
                .andExpect(jsonPath("numberOfElements").value("3"));
    }

    @Test
    @Order(5)
    void adminCanGetReportsByPartOfUsersName() throws Exception {
        UserDetails operatorUserDetails = userRepository.findUserByUsername("admin").orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(operatorUserDetails, null, operatorUserDetails.getAuthorities()));

        Report anotherUserReport = new Report(UUID.randomUUID(),
                userRepository.findUserByUsername("futureoperator").orElseThrow(),
                ReportStatus.SENT,
                "Another user's report content",
                LocalDateTime.now(),
                null,
                null);

        reportRepository.save(anotherUserReport);

        //looking for reports of Vasiliy Grigoriev
        mockMvc.perform(
                        get("/admin/reports")
                                .secure(true)
                                .param("sort", "asc")
                                .param("pageNumber", "0")
                                .param("partOfName", "Vasil")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("content").isNotEmpty())
                .andExpect(jsonPath("numberOfElements").value("3"));

        //looking for reports of Nikolay Botogin
        mockMvc.perform(
                        get("/admin/reports")
                                .secure(true)
                                .param("sort", "asc")
                                .param("pageNumber", "0")
                                .param("partOfName", "togi")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("content").isNotEmpty())
                .andExpect(jsonPath("numberOfElements").value("1"));
    }
}

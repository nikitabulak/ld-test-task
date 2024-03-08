package bul.nik.ldtesttask;

import bul.nik.ldtesttask.auth.dto.JwtResponseDto;
import bul.nik.ldtesttask.auth.dto.LoginRequestDto;
import bul.nik.ldtesttask.report.dto.EditedReportDto;
import bul.nik.ldtesttask.report.dto.NewReportDto;
import bul.nik.ldtesttask.report.model.Report;
import bul.nik.ldtesttask.report.model.ReportStatus;
import bul.nik.ldtesttask.report.repository.ReportRepository;
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

import java.util.List;

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
class UserControllerIntegrationTests {
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
    void userCanAuthenticate() throws Exception {
        LoginRequestDto userLoginRequest = new LoginRequestDto();
        userLoginRequest.setUsernameOrEmail("casualuser");
        userLoginRequest.setPassword("casualpassword");
        userLoginRequest.setRememberMe(false);

        MockHttpServletResponse response = mockMvc.perform(
                        post("/auth/login")
                                .content(objectMapper.writeValueAsString(userLoginRequest))
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
    void userCanCreateNewReport() throws Exception {
        UserDetails userDetails = userRepository.findUserByUsername("casualuser").orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        NewReportDto newReportDto = new NewReportDto();
        newReportDto.setReportContent("Test report content");

        mockMvc.perform(
                        post("/user/reports")
                                .secure(true)
                                .content(objectMapper.writeValueAsString(newReportDto))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertThat(reportRepository.findAll().get(0).getReportContent()).isEqualTo(newReportDto.getReportContent());
        assertThat(reportRepository.findAll().get(0).getReportStatus()).isEqualTo(ReportStatus.SENT);
    }

    @Test
    @Order(3)
    void userCanGetCreatedReport() throws Exception {
        UserDetails userDetails = userRepository.findUserByUsername("casualuser").orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        MockHttpServletResponse response = mockMvc.perform(
                        get("/user/reports")
                                .secure(true)
                                .param("sort", "asc")
                                .param("pageNumber", "0")
                )
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("content").isNotEmpty())
                .andExpect(jsonPath("numberOfElements").value("1"))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).contains("\"reportContent\":\"Test report content\"");
        assertThat(response.getContentAsString()).contains("\"status\":\"SENT\"");
    }

    @Test
    @Order(4)
    void userCanCreateNewDraftReport() throws Exception {
        UserDetails userDetails = userRepository.findUserByUsername("casualuser").orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        NewReportDto newReportDto = new NewReportDto();
        newReportDto.setReportContent("Test draft report content");

        mockMvc.perform(
                        post("/user/reports/drafts")
                                .secure(true)
                                .content(objectMapper.writeValueAsString(newReportDto))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Report draftReport = reportRepository.findAll().stream()
                .filter(r -> !r.getReportStatus().equals(ReportStatus.SENT))
                .findFirst()
                .orElseThrow();
        assertThat(draftReport.getReportContent()).isEqualTo(newReportDto.getReportContent());
        assertThat(draftReport.getReportStatus()).isEqualTo(ReportStatus.DRAFT);
    }

    @Test
    @Order(5)
    void userCanGetCreatedDraftReport() throws Exception {
        UserDetails userDetails = userRepository.findUserByUsername("casualuser").orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        MockHttpServletResponse response = mockMvc.perform(
                        get("/user/reports")
                                .secure(true)
                                .param("sort", "asc")
                                .param("pageNumber", "0")
                )
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("content").isNotEmpty())
                .andExpect(jsonPath("numberOfElements").value("2"))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).contains("\"reportContent\":\"Test draft report content\"");
        assertThat(response.getContentAsString()).contains("\"status\":\"DRAFT\"");
    }

    @Test
    @Order(6)
    void userCanUpdateDraftReport() throws Exception {
        UserDetails userDetails = userRepository.findUserByUsername("casualuser").orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        Report reportForUpdate = reportRepository.findAll().stream().filter(r -> r.getReportStatus()
                .equals(ReportStatus.DRAFT)).findFirst().orElseThrow();
        EditedReportDto editedReportDto = new EditedReportDto();
        editedReportDto.setId(reportForUpdate.getId());
        editedReportDto.setReportContent("Updated test draft report content");

        mockMvc.perform(
                        patch("/user/reports/drafts")
                                .secure(true)
                                .content(objectMapper.writeValueAsString(editedReportDto))
                                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
        assertThat(reportRepository.findById(reportForUpdate.getId()).orElseThrow().getReportContent())
                .isEqualTo(editedReportDto.getReportContent());
    }

    @Test
    @Order(7)
    void userCanSendDraftReport() throws Exception {
        UserDetails userDetails = userRepository.findUserByUsername("casualuser").orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        Report draftReport = reportRepository.findAll().stream().filter(r -> r.getReportStatus()
                .equals(ReportStatus.DRAFT)).findFirst().orElseThrow();

        mockMvc.perform(
                        post("/user/reports/drafts/" + draftReport.getId())
                                .secure(true))
                .andExpect(status().isOk());
        assertThat(reportRepository.findAll().stream()
                .filter(r -> !r.getReportStatus().equals(ReportStatus.SENT))
                .count())
                .isZero();
    }

    @Test
    @Order(8)
    void userCanGetOwnAcceptedAndRejectedReports() throws Exception {
        UserDetails userDetails = userRepository.findUserByUsername("casualuser").orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        List<Report> reportList = reportRepository.findAll();
        Report reportForAcception = reportList.get(0);
        Report reportForRejection = reportList.get(1);
        reportForAcception.setReportStatus(ReportStatus.ACCEPTED);
        reportForRejection.setReportStatus(ReportStatus.REJECTED);
        reportRepository.save(reportForAcception);
        reportRepository.save(reportForRejection);

        MockHttpServletResponse response = mockMvc.perform(
                        get("/user/reports")
                                .secure(true)
                                .param("sort", "asc")
                                .param("pageNumber", "0")
                )
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("content").isNotEmpty())
                .andExpect(jsonPath("numberOfElements").value("2"))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).contains("\"status\":\"ACCEPTED\"");
        assertThat(response.getContentAsString()).contains("\"status\":\"REJECTED\"");
    }
}

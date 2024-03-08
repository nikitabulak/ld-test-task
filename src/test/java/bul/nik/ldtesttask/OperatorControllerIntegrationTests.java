package bul.nik.ldtesttask;

import bul.nik.ldtesttask.auth.dto.JwtResponseDto;
import bul.nik.ldtesttask.auth.dto.LoginRequestDto;
import bul.nik.ldtesttask.report.dto.NewReportDto;
import bul.nik.ldtesttask.report.dto.ReportDto;
import bul.nik.ldtesttask.report.dto.ReportMapper;
import bul.nik.ldtesttask.report.model.Report;
import bul.nik.ldtesttask.report.model.ReportStatus;
import bul.nik.ldtesttask.report.repository.ReportRepository;
import bul.nik.ldtesttask.report.service.ReportService;
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
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureTestDatabase
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class OperatorControllerIntegrationTests {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ReportRepository reportRepository;
    @Autowired
    private ReportService reportService;

    @Test
    @Order(1)
    void operatorCanAuthenticate() throws Exception {
        LoginRequestDto operatorLoginRequest = new LoginRequestDto();
        operatorLoginRequest.setUsernameOrEmail("operator");
        operatorLoginRequest.setPassword("operatorpassword");
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
    void operatorCanGetAllReportsWithSentButNotWithDraftStatuses() throws Exception {
        UserDetails userDetails = userRepository.findUserByUsername("casualuser").orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities()));

        NewReportDto newReportDto1 = new NewReportDto();
        newReportDto1.setReportContent("Test report content 1");
        NewReportDto newReportDto2 = new NewReportDto();
        newReportDto2.setReportContent("Test report content 2");
        NewReportDto newDraftReportDto = new NewReportDto();
        newDraftReportDto.setReportContent("Test draft report content");
        reportService.createReport(newReportDto1);
        reportService.createReport(newReportDto2);
        reportService.createDraftReport(newDraftReportDto);

        UserDetails operatorUserDetails = userRepository.findUserByUsername("operator").orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(operatorUserDetails, null, operatorUserDetails.getAuthorities()));

        MockHttpServletResponse response = mockMvc.perform(
                        get("/operator/reports")
                                .secure(true)
                                .param("sort", "asc")
                                .param("pageNumber", "0")
                )
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("content").isNotEmpty())
                .andExpect(jsonPath("totalElements").value("2"))
                .andReturn().getResponse();
        assertThat(response.getStatus()).isEqualTo(200);
        assertThat(response.getContentAsString()).contains("\"reportContent\":\"Test report content 1\"");
        assertThat(response.getContentAsString()).contains("\"reportContent\":\"Test report content 2\"");
        assertThat(response.getContentAsString()).doesNotContain("\"reportContent\":\"Test draft report content\"");
        assertThat(response.getContentAsString()).contains("\"status\":\"SENT\"");
    }

    @Test
    @Order(3)
    void operatorCanGetReportsByOneWithSentButNotWithDraftStatuses() throws Exception {
        UserDetails operatorUserDetails = userRepository.findUserByUsername("operator").orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(operatorUserDetails, null, operatorUserDetails.getAuthorities()));

        UUID reportWithSentStatusId = reportService.getReports("asc", List.of(ReportStatus.SENT), 0, "")
                .stream().findFirst().orElseThrow().getId();
        UUID reportWithDraftStatusId = reportService.getReports("asc", List.of(ReportStatus.DRAFT), 0, "")
                .stream().findFirst().orElseThrow().getId();
        mockMvc.perform(
                        get("/operator/reports/" + reportWithSentStatusId)
                                .secure(true)
                                .param("sort", "asc")
                                .param("pageNumber", "0")
                )
                .andExpect(status().isOk())
                .andExpect(content().json(
                        objectMapper.writeValueAsString(
                                ReportMapper.toDto(reportRepository.findById(reportWithSentStatusId).orElseThrow()))));

        mockMvc.perform(
                        get("/operator/reports/" + reportWithDraftStatusId)
                                .secure(true)
                                .param("sort", "asc")
                                .param("pageNumber", "0"))
                .andExpect(status().isNotFound());
    }

    @Test
    @Order(4)
    void operatorCanMakeResponses() throws Exception {
        UserDetails operatorUserDetails = userRepository.findUserByUsername("operator").orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(operatorUserDetails, null, operatorUserDetails.getAuthorities()));

        List<ReportDto> sentReports = reportService.getReports("asc", List.of(ReportStatus.SENT), 0, "")
                .get().collect(Collectors.toList());

        mockMvc.perform(
                        patch("/operator/reports/" + sentReports.get(0).getId())
                                .secure(true)
                                .param("decision", "accept")
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        patch("/operator/reports/" + sentReports.get(1).getId())
                                .secure(true)
                                .param("decision", "reject")
                )
                .andExpect(status().isOk());
        assertThat(reportRepository.findById(sentReports.get(0).getId()).orElseThrow().getReportStatus())
                .isEqualTo(ReportStatus.ACCEPTED);
        assertThat(reportRepository.findById(sentReports.get(1).getId()).orElseThrow().getReportStatus())
                .isEqualTo(ReportStatus.REJECTED);
    }

    @Test
    @Order(5)
    void operatorCanNotGetAllReportsWithAcceptedAndRejectedStatuses() throws Exception {
        UserDetails operatorUserDetails = userRepository.findUserByUsername("operator").orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(operatorUserDetails, null, operatorUserDetails.getAuthorities()));

        mockMvc.perform(
                        get("/operator/reports")
                                .secure(true)
                                .param("sort", "asc")
                                .param("pageNumber", "0")
                )
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("content").isEmpty())
                .andExpect(jsonPath("totalElements").value("0"))
                .andExpect(status().isOk());
    }

    @Test
    @Order(6)
    void adminCanGetReportsByPartOfUsersName() throws Exception {
        UserDetails operatorUserDetails = userRepository.findUserByUsername("operator").orElseThrow();
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(operatorUserDetails, null, operatorUserDetails.getAuthorities()));

        Report newSentReportByVasiliyGrigoriev = new Report(UUID.randomUUID(),
                userRepository.findUserByUsername("casualuser").orElseThrow(),
                ReportStatus.SENT,
                "Vasiliy Grigoriev report content",
                LocalDateTime.now(),
                null,
                null);
        Report newSentReportByNikolayBotogin = new Report(UUID.randomUUID(),
                userRepository.findUserByUsername("futureoperator").orElseThrow(),
                ReportStatus.SENT,
                "Nikolay Botogin report content",
                LocalDateTime.now(),
                null,
                null);
        reportRepository.save(newSentReportByVasiliyGrigoriev);
        reportRepository.save(newSentReportByNikolayBotogin);

        //looking for reports of Vasiliy Grigoriev
        mockMvc.perform(
                        get("/operator/reports")
                                .secure(true)
                                .param("sort", "asc")
                                .param("pageNumber", "0")
                                .param("partOfName", "Vasil")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("content").exists())
                .andExpect(jsonPath("content").isNotEmpty())
                .andExpect(jsonPath("numberOfElements").value("1"));

        //looking for reports of Nikolay Botogin
        mockMvc.perform(
                        get("/operator/reports")
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

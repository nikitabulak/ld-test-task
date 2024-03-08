package bul.nik.ldtesttask.report.controller;

import bul.nik.ldtesttask.report.dto.EditedReportDto;
import bul.nik.ldtesttask.report.dto.NewReportDto;
import bul.nik.ldtesttask.report.dto.ReportDto;
import bul.nik.ldtesttask.report.model.ReportStatus;
import bul.nik.ldtesttask.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
public class UserReportController {
    private final ReportService reportService;

    @PostMapping("/reports")
    public void createReport(@Valid @RequestBody NewReportDto newReportDto) {
        reportService.createReport(newReportDto);
    }

    @GetMapping("/reports")
    public Page<ReportDto> getAllReports(@RequestParam(required = false, defaultValue = "asc")
                                         @Pattern(regexp = "asc|desc",
                                                 message = "only \"asc\" and \"desc\" values available for \"sort\" param") String sort,
                                         @RequestParam(required = false, defaultValue = "0") int pageNumber) {
        return reportService.getUserReports(sort, List.of(ReportStatus.SENT, ReportStatus.DRAFT, ReportStatus.ACCEPTED, ReportStatus.REJECTED), pageNumber);
    }

    @PostMapping("/reports/drafts")
    public void createDraftReport(@Valid @RequestBody NewReportDto newDraftReportDto) {
        reportService.createDraftReport(newDraftReportDto);
    }

    @PatchMapping("/reports/drafts")
    public void updateDraftReport(@Valid @RequestBody EditedReportDto userDraftReport) {
        reportService.editDraftReport(userDraftReport);
    }


    @PostMapping("/reports/drafts/{reportId}")
    public void sendDraftReport(@PathVariable UUID reportId) {
        reportService.sendDraftReport(reportId);
    }
}

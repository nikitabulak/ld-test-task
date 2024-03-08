package bul.nik.ldtesttask.report.controller;

import bul.nik.ldtesttask.report.dto.ReportDto;
import bul.nik.ldtesttask.report.model.ReportStatus;
import bul.nik.ldtesttask.report.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/operator")
public class OperatorReportController {
    private final ReportService reportService;

    @GetMapping("/reports")
    public Page<ReportDto> getAllReports(@RequestParam(required = false, defaultValue = "asc")
                                         @Pattern(regexp = "asc|desc",
                                                 message = "only \"asc\" and \"desc\" values available for \"sort\" param") String sort,
                                         @RequestParam(required = false, defaultValue = "0") int pageNumber,
                                         @RequestParam(required = false, defaultValue = "") String partOfName) {
        return reportService.getReports(sort, List.of(ReportStatus.SENT), pageNumber, partOfName);
    }

    @GetMapping("/reports/{id}")
    public ReportDto getReportById(@PathVariable UUID id) {
        return reportService.getReportForOperator(id);
    }

    @PatchMapping("/reports/{id}")
    public void makeResponse(@PathVariable UUID id,
                             @RequestParam
                             @Pattern(regexp = "accept|reject",
                                     message = "only \"accept\" and \"reject\" values available for \"decision\" param") String decision) {
        reportService.resolveReport(id, decision);
    }
}

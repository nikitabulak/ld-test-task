package bul.nik.ldtesttask.report.service;

import bul.nik.ldtesttask.report.dto.EditedReportDto;
import bul.nik.ldtesttask.report.dto.NewReportDto;
import bul.nik.ldtesttask.report.dto.ReportDto;
import bul.nik.ldtesttask.report.model.ReportStatus;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.UUID;

public interface ReportService {
    void createReport(NewReportDto userReportDto);

    Page<ReportDto> getUserReports(String sort, List<ReportStatus> permittedStatuses, int pageNumber);

    void createDraftReport(NewReportDto userDraftReportDto);

    void editDraftReport(EditedReportDto userDraftReport);

    void sendDraftReport(UUID reportId);

    Page<ReportDto> getReports(String sort, List<ReportStatus> permittedStatuses, int pageNumber, String partOfName);

    ReportDto getReportForOperator(UUID reportId);

    void resolveReport(UUID reportId, String decision);

}

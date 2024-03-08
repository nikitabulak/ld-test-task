package bul.nik.ldtesttask.report.dto;

import bul.nik.ldtesttask.report.model.Report;
import bul.nik.ldtesttask.report.model.ReportStatus;
import bul.nik.ldtesttask.user.model.User;

import java.time.LocalDateTime;

public class ReportMapper {
    private ReportMapper() {
    }

    public static ReportDto toDto(Report report) {
        return ReportDto.builder()
                .id(report.getId())
                .authorId(report.getUser().getId())
                .status(report.getReportStatus())
                .reportContent(report.getReportContent())
                .reportTime(report.getReportTime())
                .operatorId(report.getOperator() == null ? null : report.getOperator().getId())
                .statusChangeTime(report.getStatusChangeTime())
                .build();
    }

    public static Report toNewReport(NewReportDto reportDto, User author) {
        return Report.builder()
                .user(author)
                .reportStatus(ReportStatus.SENT)
                .reportContent(reportDto.getReportContent())
                .reportTime(LocalDateTime.now())
                .build();
    }

    public static Report toNewDraftReport(NewReportDto reportDto, User author) {
        return Report.builder()
                .user(author)
                .reportStatus(ReportStatus.DRAFT)
                .reportContent(reportDto.getReportContent())
                .reportTime(LocalDateTime.now())
                .build();
    }
}

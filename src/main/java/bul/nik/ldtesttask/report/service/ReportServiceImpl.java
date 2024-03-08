package bul.nik.ldtesttask.report.service;

import bul.nik.ldtesttask.exception.ReportNotFoundException;
import bul.nik.ldtesttask.report.dto.EditedReportDto;
import bul.nik.ldtesttask.report.dto.NewReportDto;
import bul.nik.ldtesttask.report.dto.ReportDto;
import bul.nik.ldtesttask.report.dto.ReportMapper;
import bul.nik.ldtesttask.report.model.Report;
import bul.nik.ldtesttask.report.model.ReportStatus;
import bul.nik.ldtesttask.report.repository.ReportRepository;
import bul.nik.ldtesttask.user.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {
    private final ReportRepository reportRepository;
    private static final String REPORT_TIME = "reportTime";

    //Methods that are available for user
    @Override
    @Transactional
    public void createReport(NewReportDto userReportDto) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Report report = ReportMapper.toNewReport(userReportDto, currentUser);
        report.setId(UUID.randomUUID());
        reportRepository.save(report);
    }

    @Override
    @Transactional
    public Page<ReportDto> getUserReports(String sort, List<ReportStatus> permittedStatuses, int pageNumber) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (sort.equals("asc")) {
            return reportRepository.findAllByUserIdAndReportStatusIn(currentUser.getId(),
                            permittedStatuses, PageRequest.of(pageNumber, 5, Sort.by(REPORT_TIME).ascending()))
                    .map(ReportMapper::toDto);
        } else {
            return reportRepository.findAllByUserIdAndReportStatusIn(currentUser.getId(),
                            permittedStatuses, PageRequest.of(pageNumber, 5, Sort.by(REPORT_TIME).descending()))
                    .map(ReportMapper::toDto);
        }
    }

    @Override
    @Transactional
    public void createDraftReport(NewReportDto userDraftReportDto) {
        User currentUser = (User) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        Report draftReport = ReportMapper.toNewDraftReport(userDraftReportDto, currentUser);
        draftReport.setId(UUID.randomUUID());
        reportRepository.save(draftReport);
    }

    @Override
    @Transactional
    public void editDraftReport(EditedReportDto userDraftReport) {
        Report report = reportRepository.findById(userDraftReport.getId()).orElseThrow(() -> new ReportNotFoundException(userDraftReport.getId()));
        report.setReportContent(userDraftReport.getReportContent());
        report.setReportTime(LocalDateTime.now());
        reportRepository.save(report);
    }

    @Override
    @Transactional
    public void sendDraftReport(UUID reportId) {
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new ReportNotFoundException(reportId));
        report.setReportStatus(ReportStatus.SENT);
        report.setReportTime(LocalDateTime.now());
        reportRepository.save(report);
    }

    //Methods that are available for operator

    @Override
    @Transactional
    public ReportDto getReportForOperator(UUID reportId) {
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new ReportNotFoundException(reportId));
        if (!report.getReportStatus().equals(ReportStatus.SENT)) {
            throw new ReportNotFoundException(reportId);
        }
        return ReportMapper.toDto(report);
    }

    @Override
    @Transactional
    public void resolveReport(UUID reportId, String decision) {
        Report report = reportRepository.findById(reportId).orElseThrow(() -> new ReportNotFoundException(reportId));
        if (!report.getReportStatus().equals(ReportStatus.SENT)) {
            throw new ReportNotFoundException(reportId);
        }
        if (decision.equals("accept")) {
            report.setReportStatus(ReportStatus.ACCEPTED);
        } else {
            report.setReportStatus(ReportStatus.REJECTED);
        }
        reportRepository.save(report);
    }

    //Methods that are available for operator and admin

    @Override
    @Transactional
    public Page<ReportDto> getReports(String sort, List<ReportStatus> permittedStatuses, int pageNumber, String partOfName) {
        if (sort.equals("asc")) {
            return reportRepository.findAllByReportStatusInAndUserNameContains(permittedStatuses,
                            PageRequest.of(pageNumber, 5, Sort.by(REPORT_TIME).ascending()), partOfName)
                    .map(ReportMapper::toDto);
        } else {
            return reportRepository.findAllByReportStatusInAndUserNameContains(permittedStatuses,
                            PageRequest.of(pageNumber, 5, Sort.by(REPORT_TIME).descending()), partOfName)
                    .map(ReportMapper::toDto);
        }
    }
}
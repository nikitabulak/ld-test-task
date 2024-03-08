package bul.nik.ldtesttask.report.repository;

import bul.nik.ldtesttask.report.model.Report;
import bul.nik.ldtesttask.report.model.ReportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<Report, UUID> {
    Page<Report> findAllByUserIdAndReportStatusIn(UUID id, List<ReportStatus> reportStatuses, Pageable pageable);

    Page<Report> findAllByReportStatusInAndUserNameContains(List<ReportStatus> reportStatuses, Pageable pageable, String partOfName);

}

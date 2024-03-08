package bul.nik.ldtesttask.report.model;

import bul.nik.ldtesttask.user.model.User;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "reports")
@Builder
public class Report {
    @Id
    @UuidGenerator
    @Column(name = "id")
    private UUID id;
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "user_id")
    private User user;
    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ReportStatus reportStatus;
    @Column(name = "report_content")
    private String reportContent;
    @Column(name = "report_time")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime reportTime;
    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinColumn(name = "operator_id")
    private User operator;
    @Column(name = "status_change_time")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime statusChangeTime;

}

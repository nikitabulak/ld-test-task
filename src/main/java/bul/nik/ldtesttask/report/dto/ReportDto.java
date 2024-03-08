package bul.nik.ldtesttask.report.dto;

import bul.nik.ldtesttask.report.model.ReportStatus;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class ReportDto {
    private UUID id;
    private UUID authorId;
    private ReportStatus status;
    @NotBlank(message = "report content is mandatory and can not be empty!")
    @Size(min = 5, max = 1000, message = "size should be between 5 an 1000 characters")
    private String reportContent;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime reportTime;
    private UUID operatorId;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME, pattern = "yyyy-MM-dd HH:mm")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    private LocalDateTime statusChangeTime;
}

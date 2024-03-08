package bul.nik.ldtesttask.report.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.util.UUID;

@Getter
@Setter
public class EditedReportDto {
    private UUID id;
    @NotBlank(message = "report content is mandatory and can not be empty!")
    @Size(min = 5, max = 1000, message = "size should be between 5 an 1000 characters")
    private String reportContent;
}

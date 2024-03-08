package bul.nik.ldtesttask.report.controller;

import bul.nik.ldtesttask.report.dto.ReportDto;
import bul.nik.ldtesttask.report.model.ReportStatus;
import bul.nik.ldtesttask.report.service.ReportService;
import bul.nik.ldtesttask.user.dto.UserDto;
import bul.nik.ldtesttask.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.Pattern;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminReportController {
    private final ReportService reportService;
    private final UserService userService;

    //Methods that are available for admin

    @GetMapping("/users")
    public Page<UserDto> getUsers(@RequestParam(required = false, defaultValue = "0") int pageNumber) {
        return userService.getUsers(pageNumber);
    }

    @PatchMapping("/users/{userId}")
    public void giveOperatorRights(@PathVariable UUID userId) {
        userService.giveOperatorRights(userId);
    }

    @GetMapping("/reports")
    public Page<ReportDto> getAllReports(@RequestParam(required = false, defaultValue = "asc")
                                         @Pattern(regexp = "asc|desc",
                                                 message = "only \"asc\" and \"desc\" values available for \"sort\" param") String sort,
                                         @RequestParam(required = false, defaultValue = "0") int pageNumber,
                                         @RequestParam(required = false, defaultValue = "") String partOfName) {
        return reportService.getReports(sort, List.of(ReportStatus.SENT, ReportStatus.ACCEPTED, ReportStatus.REJECTED),
                pageNumber, partOfName);
    }

}

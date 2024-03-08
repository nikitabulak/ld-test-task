package bul.nik.ldtesttask.exception;

import java.util.UUID;

public class ReportNotFoundException extends RuntimeException {
    public ReportNotFoundException(UUID feedbackId) {
        super(String.format("Report with id = %s not found", feedbackId));
    }
}

package bul.nik.ldtesttask.dadata.controller;

import bul.nik.ldtesttask.dadata.service.DadataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/dadata")
public class DadataController {
    private final DadataService dadataService;

    @PostMapping("/check_phone_number/{userId}")
    public void createReport(@PathVariable UUID userId) {
        dadataService.checkPhoneNumber(userId);
    }
}

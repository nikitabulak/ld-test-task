package bul.nik.ldtesttask.dadata.service;

import bul.nik.ldtesttask.dadata.dto.DadataFullResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(value = "numberchecker", url = "https://cleaner.dadata.ru/api/v1/clean/phone")
public interface NumberChecker {

    @PostMapping(produces = "application/json", consumes = "application/json")
    List<DadataFullResponse> getPhoneInfo(@RequestBody String[] number,
                                          @RequestHeader(value = HttpHeaders.AUTHORIZATION) String apiKey,
                                          @RequestHeader("X-Secret") String secretKey);
}

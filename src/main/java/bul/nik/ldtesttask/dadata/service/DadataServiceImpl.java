package bul.nik.ldtesttask.dadata.service;

import bul.nik.ldtesttask.dadata.dto.DadataFullResponse;
import bul.nik.ldtesttask.dadata.model.PhoneNumberData;
import bul.nik.ldtesttask.dadata.repository.DadataRepository;
import bul.nik.ldtesttask.exception.PhoneNumberCheckException;
import bul.nik.ldtesttask.exception.UserNotFoundException;
import bul.nik.ldtesttask.user.model.User;
import bul.nik.ldtesttask.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class DadataServiceImpl implements DadataService {
    private final UserRepository userRepository;
    private final NumberChecker numberChecker;
    private final DadataRepository dadataRepository;

    @Value("${app.dadata.apikey}")
    private String apiKey;
    @Value("${app.dadata.secretkey}")
    private String secretKey;

    @Override
    @Transactional
    public void checkPhoneNumber(UUID userId) {
        User user = userRepository.findUserById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        if (user.getPhoneNumberData() == null) {
            DadataFullResponse dadataResponse = numberChecker.getPhoneInfo(new String[]{user.getPhoneNumber()},
                    "Token " + apiKey, secretKey).get(0);
            if (dadataResponse.getType().equals("Мобильный")) {
                PhoneNumberData phoneNumberData = new PhoneNumberData(UUID.randomUUID(), user, dadataResponse.getCountry_code(),
                        dadataResponse.getCity_code(), dadataResponse.getNumber());
                user.setPhoneNumberData(phoneNumberData);
                userRepository.save(user);
                dadataRepository.save(phoneNumberData);
            } else {
                throw new PhoneNumberCheckException(user.getPhoneNumber(), "Номер не является мобильным");
            }
        }
    }
}
package bul.nik.ldtesttask.dadata;

import bul.nik.ldtesttask.dadata.dto.PhoneNumberDataDto;
import bul.nik.ldtesttask.dadata.model.PhoneNumberData;

public class PhoneNumberDataMapper {
    private PhoneNumberDataMapper() {
    }

    public static PhoneNumberDataDto toPhoneNumberDataDto(PhoneNumberData data) {
        return PhoneNumberDataDto.builder()
                .countryCode(data.getCountryCode())
                .cityCode(data.getCityCode())
                .phone(data.getPhone())
                .build();
    }
}

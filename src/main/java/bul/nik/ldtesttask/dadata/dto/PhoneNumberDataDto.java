package bul.nik.ldtesttask.dadata.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import javax.persistence.Column;

@Getter
@AllArgsConstructor
@ToString
@Builder
public class PhoneNumberDataDto {
    @Column(name = "country_code")
    private String countryCode;
    @Column(name = "city_code")
    private String cityCode;
    @Column(name = "phone")
    private String phone;

}

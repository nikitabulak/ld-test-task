package bul.nik.ldtesttask.dadata.dto;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class DadataFullResponse {
    private String source;
    private String type;
    private String phone;
    private String country_code;
    private String city_code;
    private String number;
    private String extension;
    private String provider;
    private String country;
    private String region;
    private String city;
    private String timezone;
    private int qc;
    private int qc_conflict;


}

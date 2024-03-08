package bul.nik.ldtesttask.user.dto;

import bul.nik.ldtesttask.dadata.dto.PhoneNumberDataDto;
import bul.nik.ldtesttask.user.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

import java.util.Set;

@Getter
@AllArgsConstructor
@ToString
@Builder
public class UserDto {
    private String username;
    private String email;
    private String phoneNumber;
    private PhoneNumberDataDto phoneNumberData;
    private Set<Role> roles;
    private String name;
}

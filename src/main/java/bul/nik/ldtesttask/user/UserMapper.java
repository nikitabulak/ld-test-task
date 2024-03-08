package bul.nik.ldtesttask.user;

import bul.nik.ldtesttask.dadata.PhoneNumberDataMapper;
import bul.nik.ldtesttask.user.dto.UserDto;
import bul.nik.ldtesttask.user.model.User;

public class UserMapper {
    private UserMapper() {
    }

    public static UserDto toUserDto(User user) {
        return UserDto.builder()
                .name(user.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhoneNumber())
                .phoneNumberData(
                        user.getPhoneNumberData() == null ? null :
                                PhoneNumberDataMapper.toPhoneNumberDataDto(user.getPhoneNumberData()))
                .roles(user.getRoles())
                .build();
    }
}

package bul.nik.ldtesttask.user.service;

import bul.nik.ldtesttask.user.dto.UserDto;
import org.springframework.data.domain.Page;

import java.util.UUID;

public interface UserService {
    Page<UserDto> getUsers(int pageNumber);

    void giveOperatorRights(UUID userId);
}

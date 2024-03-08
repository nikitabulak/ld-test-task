package bul.nik.ldtesttask.user.service;

import bul.nik.ldtesttask.exception.UserNotFoundException;
import bul.nik.ldtesttask.user.UserMapper;
import bul.nik.ldtesttask.user.dto.UserDto;
import bul.nik.ldtesttask.user.model.ERole;
import bul.nik.ldtesttask.user.model.User;
import bul.nik.ldtesttask.user.repository.RoleRepository;
import bul.nik.ldtesttask.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService, UserDetailsService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public Page<UserDto> getUsers(int pageNumber) {
        return userRepository.findAll(PageRequest.of(pageNumber, 5)).map(UserMapper::toUserDto);
    }

    @Override
    @Transactional
    public void giveOperatorRights(UUID userId) {
        User user = userRepository.findUserById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        user.getRoles().add(roleRepository.findRoleByName(ERole.ROLE_OPERATOR).orElseThrow());
        userRepository.save(user);
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        return userRepository.findUserByUsername(usernameOrEmail).orElseGet(
                () -> userRepository.findUserByEmail(usernameOrEmail)
                        .orElseThrow(() -> new UsernameNotFoundException(
                                String.format("User with username %s not found", usernameOrEmail))));
    }
}

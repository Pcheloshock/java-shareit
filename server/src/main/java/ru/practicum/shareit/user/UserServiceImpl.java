package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDto createUser(UserDto userDto) {
        log.info("Создание пользователя");
        
        // Проверка на уникальность email
        if (userDto.getEmail() != null) {
            userRepository.findByEmail(userDto.getEmail())
                .ifPresent(user -> {
                    throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
                });
        }
        
        User user = UserMapper.toUser(userDto);
        User savedUser = userRepository.save(user);
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    @Transactional
    public UserDto updateUser(Long userId, UserDto userDto) {
        log.info("Обновление пользователя ID: {}", userId);
        
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        // Проверка на уникальность email при обновлении
        if (userDto.getEmail() != null && !userDto.getEmail().equals(user.getEmail())) {
            userRepository.findByEmail(userDto.getEmail())
                .ifPresent(existingUser -> {
                    throw new ConflictException("Пользователь с email " + userDto.getEmail() + " уже существует");
                });
            user.setEmail(userDto.getEmail());
        }

        if (userDto.getName() != null) {
            user.setName(userDto.getName());
        }

        return UserMapper.toUserDto(user);
    }

    @Override
    public UserDto getUserById(Long userId) {
        log.info("Получение пользователя ID: {}", userId);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        log.info("Получение всех пользователей");
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteUser(Long userId) {
        log.info("Удаление пользователя ID: {}", userId);
        userRepository.deleteById(userId);
    }
}

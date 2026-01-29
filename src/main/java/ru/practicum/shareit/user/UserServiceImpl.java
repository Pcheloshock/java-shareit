package ru.practicum.shareit.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.ConflictException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.user.dto.UserDto;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final InMemoryUserRepository userRepository;

    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@(.+)$");

    private void validateUser(UserDto userDto, boolean isUpdate) {
        if (!isUpdate && (userDto.getName() == null || userDto.getName().isBlank())) {
            throw new ValidationException("Имя не может быть пустым");
        }

        if (!isUpdate && (userDto.getEmail() == null || userDto.getEmail().isBlank())) {
            throw new ValidationException("Email не может быть пустым");
        }

        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            if (!EMAIL_PATTERN.matcher(userDto.getEmail()).matches()) {
                throw new ValidationException("Некорректный формат email");
            }
        }
    }

    private void checkEmailUniqueness(String email, Long userId) {
        for (User user : userRepository.findAll()) {
            if (user.getEmail().equals(email) && !user.getId().equals(userId)) {
                throw new ConflictException("Пользователь с email " + email + " уже существует");
            }
        }
    }

    @Override
    public UserDto createUser(UserDto userDto) {
        validateUser(userDto, false);
        checkEmailUniqueness(userDto.getEmail(), null);

        User user = UserMapper.toUser(userDto);
        User savedUser = userRepository.save(user);
        return UserMapper.toUserDto(savedUser);
    }

    @Override
    public UserDto updateUser(Long userId, UserDto userDto) {
        validateUser(userDto, true);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));

        boolean emailChanged = userDto.getEmail() != null &&
                !userDto.getEmail().equals(user.getEmail());

        if (emailChanged) {
            checkEmailUniqueness(userDto.getEmail(), userId);
        }

        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            user.setEmail(userDto.getEmail());
        }

        User updatedUser = userRepository.save(user);
        return UserMapper.toUserDto(updatedUser);
    }

    @Override
    public UserDto getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + userId + " не найден"));
        return UserMapper.toUserDto(user);
    }

    @Override
    public List<UserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(UserMapper::toUserDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        userRepository.deleteById(userId);
    }
}
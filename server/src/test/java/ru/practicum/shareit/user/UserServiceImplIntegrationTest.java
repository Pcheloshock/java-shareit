package ru.practicum.shareit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.dto.UserDto;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class UserServiceImplIntegrationTest {

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private UserDto userDto;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        userDto = UserDto.builder()
                .name("John Doe")
                .email("john@example.com")
                .build();
    }

    @Test
    void createUser_ValidData_ReturnsUserDto() {
        UserDto created = userService.createUser(userDto);

        assertNotNull(created.getId());
        assertEquals("John Doe", created.getName());
        assertEquals("john@example.com", created.getEmail());
    }

    @Test
    void getUserById_ValidId_ReturnsUser() {
        UserDto created = userService.createUser(userDto);
        UserDto found = userService.getUserById(created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals(created.getName(), found.getName());
    }

    @Test
    void getUserById_InvalidId_ThrowsException() {
        assertThrows(NotFoundException.class, () -> userService.getUserById(999L));
    }

    @Test
    void getAllUsers_ReturnsList() {
        userService.createUser(userDto);
        userService.createUser(UserDto.builder().name("Jane Doe").email("jane@example.com").build());

        assertEquals(2, userService.getAllUsers().size());
    }

    @Test
    void updateUser_ValidData_UpdatesUser() {
        UserDto created = userService.createUser(userDto);
        UserDto updateDto = UserDto.builder().name("Updated Name").build();

        UserDto updated = userService.updateUser(created.getId(), updateDto);

        assertEquals("Updated Name", updated.getName());
        assertEquals(created.getEmail(), updated.getEmail());
    }

    @Test
    void deleteUser_RemovesUser() {
        UserDto created = userService.createUser(userDto);
        userService.deleteUser(created.getId());

        assertThrows(NotFoundException.class, () -> userService.getUserById(created.getId()));
    }
}

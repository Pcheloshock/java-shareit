package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.ItemRequestRepository;
import ru.practicum.shareit.item.ItemRequestService;
import ru.practicum.shareit.item.dto.CreateItemRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ItemRequestRepository requestRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();

        user1 = userRepository.save(User.builder()
                .name("User 1")
                .email("user1@test.com")
                .build());

        user2 = userRepository.save(User.builder()
                .name("User 2")
                .email("user2@test.com")
                .build());
    }

    @Test
    void createRequest_ValidData_ShouldCreateRequest() {
        CreateItemRequestDto createDto = new CreateItemRequestDto();
        createDto.setDescription("Нужна дрель");

        ItemRequestDto result = requestService.createRequest(user1.getId(), createDto);

        assertNotNull(result);
        assertNotNull(result.getId());
        assertEquals("Нужна дрель", result.getDescription());
        assertNotNull(result.getCreated());
        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void createRequest_EmptyDescription_ShouldThrowValidationException() {
        CreateItemRequestDto createDto = new CreateItemRequestDto();
        createDto.setDescription("   ");

        assertThrows(ValidationException.class,
                () -> requestService.createRequest(user1.getId(), createDto));
    }

    @Test
    void createRequest_UserNotFound_ShouldThrowNotFoundException() {
        CreateItemRequestDto createDto = new CreateItemRequestDto();
        createDto.setDescription("Нужна дрель");

        assertThrows(NotFoundException.class,
                () -> requestService.createRequest(999L, createDto));
    }

    @Test
    void getUserRequests_ShouldReturnUserRequests() {
        CreateItemRequestDto createDto = new CreateItemRequestDto();
        createDto.setDescription("Нужна дрель");
        requestService.createRequest(user1.getId(), createDto);

        createDto.setDescription("Нужна пила");
        requestService.createRequest(user1.getId(), createDto);

        var result = requestService.getUserRequests(user1.getId());

        assertEquals(2, result.size());
        assertEquals("Нужна пила", result.get(0).getDescription()); // sorted by created desc
        assertEquals("Нужна дрель", result.get(1).getDescription());
    }

    @Test
    void getAllRequests_ShouldReturnOtherUsersRequests() {
        CreateItemRequestDto createDto = new CreateItemRequestDto();
        createDto.setDescription("Нужна дрель от user1");
        requestService.createRequest(user1.getId(), createDto);

        createDto.setDescription("Нужна пила от user1");
        requestService.createRequest(user1.getId(), createDto);

        createDto.setDescription("Нужна отвертка от user2");
        requestService.createRequest(user2.getId(), createDto);

        var result = requestService.getAllRequests(user1.getId());

        assertEquals(1, result.size());
        assertEquals("Нужна отвертка от user2", result.get(0).getDescription());
    }

    @Test
    void getRequestById_ShouldReturnRequestWithItems() {
        CreateItemRequestDto createDto = new CreateItemRequestDto();
        createDto.setDescription("Нужна дрель");
        ItemRequestDto created = requestService.createRequest(user1.getId(), createDto);

        var result = requestService.getRequestById(user2.getId(), created.getId());

        assertNotNull(result);
        assertEquals(created.getId(), result.getId());
        assertEquals("Нужна дрель", result.getDescription());
    }

    @Test
    void getRequestById_NotFound_ShouldThrowNotFoundException() {
        assertThrows(NotFoundException.class,
                () -> requestService.getRequestById(user1.getId(), 999L));
    }
}
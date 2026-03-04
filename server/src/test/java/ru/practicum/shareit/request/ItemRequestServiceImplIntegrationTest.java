package ru.practicum.shareit.request;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemRequestServiceImplIntegrationTest {

    @Autowired
    private ItemRequestService requestService;

    @Autowired
    private UserRepository userRepository;

    private User user1;
    private User user2;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        user1 = userRepository.save(User.builder()
                .name("User1")
                .email("user1@example.com")
                .build());
        user2 = userRepository.save(User.builder()
                .name("User2")
                .email("user2@example.com")
                .build());
    }

    @Test
    void createRequest_ValidData_ReturnsRequestDto() {
        CreateItemRequestDto createDto = new CreateItemRequestDto("Need a drill");

        ItemRequestDto created = requestService.createRequest(user1.getId(), createDto);

        assertNotNull(created.getId());
        assertEquals("Need a drill", created.getDescription());
        assertNotNull(created.getCreated());
    }

    @Test
    void getUserRequests_ReturnsUserRequests() {
        requestService.createRequest(user1.getId(), new CreateItemRequestDto("Request 1"));
        requestService.createRequest(user1.getId(), new CreateItemRequestDto("Request 2"));
        requestService.createRequest(user2.getId(), new CreateItemRequestDto("Request 3"));

        List<ItemRequestDto> requests = requestService.getUserRequests(user1.getId());

        assertEquals(2, requests.size());
    }

    @Test
    void getAllRequests_ReturnsOtherUsersRequests() {
        requestService.createRequest(user1.getId(), new CreateItemRequestDto("Request 1"));
        requestService.createRequest(user2.getId(), new CreateItemRequestDto("Request 2"));
        requestService.createRequest(user2.getId(), new CreateItemRequestDto("Request 3"));

        List<ItemRequestDto> requests = requestService.getAllRequests(user1.getId());

        assertEquals(2, requests.size());
    }

    @Test
    void getRequestById_ValidId_ReturnsRequest() {
        ItemRequestDto created = requestService.createRequest(user1.getId(),
                new CreateItemRequestDto("Need a drill"));

        ItemRequestDto found = requestService.getRequestById(user2.getId(), created.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("Need a drill", found.getDescription());
    }

    @Test
    void getRequestById_InvalidId_ThrowsException() {
        assertThrows(NotFoundException.class,
                () -> requestService.getRequestById(user1.getId(), 999L));
    }
}

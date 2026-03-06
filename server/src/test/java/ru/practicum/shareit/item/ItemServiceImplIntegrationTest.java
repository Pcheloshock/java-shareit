package ru.practicum.shareit.item;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.UserService;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class ItemServiceImplIntegrationTest {

    @Autowired
    private ItemService itemService;

    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    private User owner;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        UserDto ownerDto = UserDto.builder()
                .name("Owner")
                .email("owner@example.com")
                .build();
        owner = userRepository.save(User.builder()
                .name(ownerDto.getName())
                .email(ownerDto.getEmail())
                .build());
    }

    @Test
    void createItem_ValidData_ReturnsItemDto() {
        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build();

        ItemDto created = itemService.createItem(owner.getId(), itemDto);

        assertNotNull(created.getId());
        assertEquals("Drill", created.getName());
        assertTrue(created.getAvailable());
    }

    @Test
    void getItemWithBookingsById_ValidId_ReturnsItem() {
        ItemDto itemDto = ItemDto.builder()
                .name("Drill")
                .description("Powerful drill")
                .available(true)
                .build();

        ItemDto created = itemService.createItem(owner.getId(), itemDto);
        ItemWithBookingsDto found = itemService.getItemWithBookingsById(created.getId(), owner.getId());

        assertEquals(created.getId(), found.getId());
        assertEquals("Drill", found.getName());
    }

    @Test
    void getItemsByOwner_ReturnsItems() {
        ItemDto item1 = ItemDto.builder().name("Drill").description("Powerful drill").available(true).build();
        ItemDto item2 = ItemDto.builder().name("Hammer").description("Heavy hammer").available(true).build();

        itemService.createItem(owner.getId(), item1);
        itemService.createItem(owner.getId(), item2);

        List<ItemDto> items = itemService.getItemsByOwner(owner.getId());

        assertEquals(2, items.size());
    }

    @Test
    void searchItems_ReturnsMatchingItems() {
        ItemDto drill = ItemDto.builder().name("Drill").description("Powerful Makita drill").available(true).build();
        ItemDto hammer = ItemDto.builder().name("Hammer").description("Heavy hammer").available(true).build();

        itemService.createItem(owner.getId(), drill);
        itemService.createItem(owner.getId(), hammer);

        List<ItemDto> results = itemService.searchItems("drill");

        assertEquals(1, results.size());
        assertEquals("Drill", results.get(0).getName());
    }

    @Test
    void updateItem_ValidData_UpdatesItem() {
        ItemDto itemDto = ItemDto.builder().name("Drill").description("Powerful drill").available(true).build();
        ItemDto created = itemService.createItem(owner.getId(), itemDto);

        ItemDto updateDto = ItemDto.builder().name("Updated Drill").build();
        ItemDto updated = itemService.updateItem(owner.getId(), created.getId(), updateDto);

        assertEquals("Updated Drill", updated.getName());
        assertEquals("Powerful drill", updated.getDescription());
    }
}

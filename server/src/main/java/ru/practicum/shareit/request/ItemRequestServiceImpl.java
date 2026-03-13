package ru.practicum.shareit.request;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.item.Item;
import ru.practicum.shareit.item.ItemRepository;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.request.dto.CreateItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestDto;
import ru.practicum.shareit.request.dto.ItemRequestWithItemsDto;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemRequestServiceImpl implements ItemRequestService {
    private final ItemRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public ItemRequestDto createRequest(Long userId, CreateItemRequestDto createDto) {
        log.info("Создание запроса на вещь пользователем ID: {}", userId);

        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest request = ItemRequest.builder()
                .description(createDto.getDescription())
                .requester(requester)
                .created(LocalDateTime.now())
                .build();

        ItemRequest savedRequest = requestRepository.save(request);
        log.info("Запрос на вещь успешно создан с ID: {}", savedRequest.getId());

        return mapToDto(savedRequest);
    }

    @Override
    public List<ItemRequestWithItemsDto> getUserRequests(Long userId) {
        log.info("Получение запросов пользователя ID: {}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<ItemRequest> requests = requestRepository.findByRequesterIdOrderByCreatedDesc(userId);

        return requests.stream()
                .map(this::mapToWithItemsDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestWithItemsDto> getAllRequests(Long userId) {
        log.info("Получение всех запросов кроме запросов пользователя ID: {}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<ItemRequest> requests = requestRepository.findAllOtherRequests(userId);

        return requests.stream()
                .map(this::mapToWithItemsDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestWithItemsDto getRequestById(Long userId, Long requestId) {
        log.info("Получение запроса ID: {} пользователем ID: {}", requestId, userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException("Запрос не найден"));

        return mapToWithItemsDto(request);
    }

    private ItemRequestDto mapToDto(ItemRequest request) {
        return ItemRequestDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .build();
    }

    private ItemRequestWithItemsDto mapToWithItemsDto(ItemRequest request) {
        List<Item> items = itemRepository.findAll().stream()
                .filter(item -> item.getRequest() != null && item.getRequest().getId().equals(request.getId()))
                .collect(Collectors.toList());

        List<ItemDto> itemDtos = items.stream()
                .map(this::mapToItemDto)
                .collect(Collectors.toList());

        return ItemRequestWithItemsDto.builder()
                .id(request.getId())
                .description(request.getDescription())
                .created(request.getCreated())
                .items(itemDtos)
                .build();
    }

    private ItemDto mapToItemDto(Item item) {
        return ItemDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequest() != null ? item.getRequest().getId() : null)
                .build();
    }
}

package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CreateItemRequestDto;
import ru.practicum.shareit.item.dto.ItemRequestDto;
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

    private static final Sort SORT_BY_CREATED_DESC = Sort.by(Sort.Direction.DESC, "created");

    @Override
    @Transactional
    public ItemRequestDto createRequest(Long userId, CreateItemRequestDto createDto) {
        log.info("Создание запроса на вещь пользователем ID: {}", userId);

        if (createDto.getDescription() == null || createDto.getDescription().isBlank()) {
            throw new ValidationException("Описание запроса не может быть пустым");
        }

        User requester = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest request = ItemRequestMapper.toItemRequest(createDto, requester, LocalDateTime.now());
        ItemRequest savedRequest = requestRepository.save(request);

        log.info("Запрос на вещь успешно создан с ID: {}", savedRequest.getId());
        return ItemRequestMapper.toItemRequestDto(savedRequest);
    }

    @Override
    public List<ItemRequestDto> getUserRequests(Long userId) {
        log.info("Получение запросов пользователя ID: {}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<ItemRequest> requests = requestRepository.findByRequesterIdWithItems(userId, SORT_BY_CREATED_DESC);

        log.info("Найдено {} запросов у пользователя ID: {}", requests.size(), userId);
        return requests.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemRequestDto> getAllRequests(Long userId) {
        log.info("Получение всех запросов кроме запросов пользователя ID: {}", userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<ItemRequest> requests = requestRepository.findAllExceptUserWithItems(userId, SORT_BY_CREATED_DESC);

        log.info("Найдено {} запросов других пользователей", requests.size());
        return requests.stream()
                .map(ItemRequestMapper::toItemRequestDto)
                .collect(Collectors.toList());
    }

    @Override
    public ItemRequestDto getRequestById(Long userId, Long requestId) {
        log.info("Получение запроса ID: {} пользователем ID: {}", requestId, userId);

        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest request = requestRepository.findByIdWithItems(requestId);
        if (request == null) {
            throw new NotFoundException("Запрос не найден");
        }

        return ItemRequestMapper.toItemRequestDto(request);
    }
}
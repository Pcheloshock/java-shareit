package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingMapper;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.item.model.Item;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CommentRepository commentRepository;

    @Override
    @Transactional
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        log.info("Создание вещи пользователем ID: {}", userId);

        validateItem(itemDto, false);

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = ItemMapper.toItem(itemDto, owner);
        Item savedItem = itemRepository.save(item);

        log.info("Вещь успешно создана с ID: {}", savedItem.getId());
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Обновление вещи ID: {} пользователем ID: {}", itemId, userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getOwner().getId().equals(userId)) {
            throw new NotFoundException("Только владелец может редактировать вещь");
        }

        if (itemDto.getName() != null && !itemDto.getName().isBlank()) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null && !itemDto.getDescription().isBlank()) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }

        Item updatedItem = itemRepository.save(item);
        log.info("Вещь ID: {} успешно обновлена", itemId);

        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemWithBookingsDto getItemWithBookingsById(Long itemId, Long userId) {
        log.info("Получение информации о вещи ID: {} пользователем ID: {}", itemId, userId);

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        ItemWithBookingsDto dto = toItemWithBookingsDto(item);

        // Если пользователь - владелец вещи, добавляем информацию о бронированиях
        if (item.getOwner().getId().equals(userId)) {
            addBookingInfo(dto, itemId);
        }

        // Добавляем комментарии для всех пользователей
        addCommentsInfo(dto, itemId);

        log.info("Информация о вещи ID: {} успешно получена", itemId);
        return dto;
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId) {
        log.info("Получение всех вещей владельца ID: {}", ownerId);

        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<Item> items = itemRepository.findByOwnerId(ownerId);
        log.info("Найдено {} вещей у владельца ID: {}", items.size(), ownerId);

        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemWithBookingsDto> getItemsWithBookingsByOwner(Long ownerId) {
        log.info("Получение всех вещей с бронированиями владельца ID: {}", ownerId);

        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        // Получаем все вещи владельца
        List<Item> items = itemRepository.findByOwnerId(ownerId);

        if (items.isEmpty()) {
            return List.of();
        }

        List<Long> itemIds = items.stream()
                .map(Item::getId)
                .collect(Collectors.toList());

        LocalDateTime now = LocalDateTime.now();

        // Получаем все последние бронирования для всех вещей одним запросом
        List<Booking> lastBookings = bookingRepository.findLastBookingsForItems(itemIds, now);

        // Получаем все следующие бронирования для всех вещей одним запросом
        List<Booking> nextBookings = bookingRepository.findNextBookingsForItems(itemIds, now);

        // Получаем все комментарии для всех вещей одним запросом
        List<Comment> comments = commentRepository.findByItemIdInWithAuthor(itemIds);

        // Группируем бронирования по itemId
        Map<Long, List<Booking>> lastBookingsByItemId = lastBookings.stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));

        Map<Long, List<Booking>> nextBookingsByItemId = nextBookings.stream()
                .collect(Collectors.groupingBy(b -> b.getItem().getId()));

        // Группируем комментарии по itemId
        Map<Long, List<Comment>> commentsByItemId = comments.stream()
                .collect(Collectors.groupingBy(c -> c.getItem().getId()));

        // Создаем DTO с данными
        List<ItemWithBookingsDto> dtos = items.stream()
                .map(item -> {
                    ItemWithBookingsDto dto = toItemWithBookingsDto(item);

                    // Добавляем последнее бронирование
                    List<Booking> itemLastBookings = lastBookingsByItemId.get(item.getId());
                    if (itemLastBookings != null && !itemLastBookings.isEmpty()) {
                        dto.setLastBooking(BookingMapper.toSimpleDto(itemLastBookings.get(0)));
                    }

                    // Добавляем следующее бронирование
                    List<Booking> itemNextBookings = nextBookingsByItemId.get(item.getId());
                    if (itemNextBookings != null && !itemNextBookings.isEmpty()) {
                        dto.setNextBooking(BookingMapper.toSimpleDto(itemNextBookings.get(0)));
                    }

                    // Добавляем комментарии
                    List<Comment> itemComments = commentsByItemId.get(item.getId());
                    if (itemComments != null) {
                        dto.setComments(CommentMapper.toCommentDtoList(itemComments));
                    } else {
                        dto.setComments(List.of());
                    }

                    return dto;
                })
                .collect(Collectors.toList());

        log.info("Найдено {} вещей с бронированиями у владельца ID: {}", dtos.size(), ownerId);
        return dtos;
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        log.info("Поиск вещей по тексту: {}", text);

        if (text == null || text.isBlank()) {
            return List.of();
        }

        List<Item> items = itemRepository.searchAvailableItems(text);
        log.info("Найдено {} вещей по запросу: {}", items.size(), text);

        return items.stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CreateCommentDto commentDto) {
        log.info("Добавление комментария к вещи ID: {} пользователем ID: {}", itemId, userId);

        // Валидация текста комментария выполняется в контроллере через @Valid
        // Здесь проверяем только бизнес-логику

        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        LocalDateTime now = LocalDateTime.now();
        boolean hasBookedAndCompleted = bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                userId, itemId, now, BookingStatus.APPROVED);

        if (!hasBookedAndCompleted) {
            throw new ValidationException("Пользователь может оставить комментарий только после завершения аренды");
        }

        Comment comment = CommentMapper.toComment(commentDto, item, author, now);
        Comment savedComment = commentRepository.save(comment);

        log.info("Комментарий успешно добавлен к вещи ID: {}", itemId);
        return CommentMapper.toCommentDto(savedComment);
    }

    private void validateItem(ItemDto itemDto, boolean isUpdate) {
        if (!isUpdate && (itemDto.getName() == null || itemDto.getName().isBlank())) {
            throw new ValidationException("Название не может быть пустым");
        }

        if (!isUpdate && (itemDto.getDescription() == null || itemDto.getDescription().isBlank())) {
            throw new ValidationException("Описание не может быть пустым");
        }

        if (!isUpdate && itemDto.getAvailable() == null) {
            throw new ValidationException("Статус доступности должен быть указан");
        }
    }

    private ItemWithBookingsDto toItemWithBookingsDto(Item item) {
        return ItemWithBookingsDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .available(item.getAvailable())
                .requestId(item.getRequestId())
                .build();
    }

    private void addBookingInfo(ItemWithBookingsDto dto, Long itemId) {
        addBookingInfo(dto, itemId, LocalDateTime.now());
    }

    private void addBookingInfo(ItemWithBookingsDto dto, Long itemId, LocalDateTime now) {
        List<Booking> lastBookings = bookingRepository.findLastBookings(itemId, now);
        if (!lastBookings.isEmpty()) {
            dto.setLastBooking(BookingMapper.toSimpleDto(lastBookings.get(0)));
        }

        List<Booking> nextBookings = bookingRepository.findNextBookings(itemId, now);
        if (!nextBookings.isEmpty()) {
            dto.setNextBooking(BookingMapper.toSimpleDto(nextBookings.get(0)));
        }
    }

    private void addCommentsInfo(ItemWithBookingsDto dto, Long itemId) {
        List<CommentDto> comments = CommentMapper.toCommentDtoList(commentRepository.findByItemId(itemId));
        dto.setComments(comments);
    }
}
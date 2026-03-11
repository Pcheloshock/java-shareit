package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingStatus;
import ru.practicum.shareit.booking.dto.BookingDto;
import ru.practicum.shareit.booking.dto.BookingMapper;
import ru.practicum.shareit.exception.ForbiddenException;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidationException;
import ru.practicum.shareit.item.dto.CommentDto;
import ru.practicum.shareit.item.dto.CreateCommentDto;
import ru.practicum.shareit.item.dto.ItemDto;
import ru.practicum.shareit.item.dto.ItemWithBookingsDto;
import ru.practicum.shareit.item.model.Comment;
import ru.practicum.shareit.request.ItemRequest;
import ru.practicum.shareit.request.ItemRequestRepository;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
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
    private final ItemRequestRepository requestRepository;

    @Override
    @Transactional
    public ItemDto createItem(Long userId, ItemDto itemDto) {
        log.info("Создание вещи пользователем ID: {}", userId);
        
        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        ItemRequest request = null;
        if (itemDto.getRequestId() != null) {
            request = requestRepository.findById(itemDto.getRequestId())
                    .orElseThrow(() -> new NotFoundException("Запрос не найден"));
        }

        Item item = ItemMapper.toItem(itemDto, owner, request);
        Item savedItem = itemRepository.save(item);
        log.info("Вещь создана с ID: {}", savedItem.getId());
        
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
        log.info("Обновление вещи ID: {} пользователем ID: {}", itemId, userId);
        
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        if (!item.getOwner().getId().equals(userId)) {
            throw new ForbiddenException("Нет прав на редактирование");
        }

        if (itemDto.getName() != null) {
            item.setName(itemDto.getName());
        }
        if (itemDto.getDescription() != null) {
            item.setDescription(itemDto.getDescription());
        }
        if (itemDto.getAvailable() != null) {
            item.setAvailable(itemDto.getAvailable());
        }
        
        Item updatedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(updatedItem);
    }

    @Override
    public ItemWithBookingsDto getItemWithBookingsById(Long itemId, Long userId) {
        log.info("Получение вещи ID: {} пользователем ID: {}", itemId, userId);
        
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        ItemWithBookingsDto dto = ItemMapper.toItemWithBookingsDto(item);

        // Добавляем информацию о бронированиях только для владельца
        if (item.getOwner().getId().equals(userId)) {
            LocalDateTime now = LocalDateTime.now();
            
            List<Booking> lastBookings = bookingRepository.findLastBookingsForItem(itemId, now);
            if (!lastBookings.isEmpty()) {
                BookingDto lastBookingDto = BookingMapper.toSimpleDto(lastBookings.get(0));
                dto.setLastBooking(lastBookingDto);
            }
            
            List<Booking> nextBookings = bookingRepository.findNextBookingsForItem(itemId, now);
            if (!nextBookings.isEmpty()) {
                BookingDto nextBookingDto = BookingMapper.toSimpleDto(nextBookings.get(0));
                dto.setNextBooking(nextBookingDto);
            }
        }

        List<CommentDto> comments = ItemMapper.toCommentDtoList(
                commentRepository.findByItemId(itemId));
        dto.setComments(comments);

        return dto;
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId) {
        log.info("Получение вещей владельца ID: {}", ownerId);
        
        return itemRepository.findByOwnerId(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        log.info("Поиск вещей по тексту: {}", text);
        
        if (text == null || text.isBlank()) {
            return List.of();
        }
        
        String lowerText = text.toLowerCase();
        return itemRepository.findAll().stream()
                .filter(item -> item.getAvailable() &&
                        (item.getName().toLowerCase().contains(lowerText) ||
                         item.getDescription().toLowerCase().contains(lowerText)))
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CreateCommentDto commentDto) {
        log.info("Добавление комментария к вещи ID: {} пользователем ID: {}", itemId, userId);

        // Проверяем пользователя
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        // Проверяем вещь
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        // Проверяем, что пользователь брал эту вещь в аренду и бронирование завершено
        LocalDateTime now = LocalDateTime.now();
        boolean hasCompletedBooking = bookingRepository.existsByBookerIdAndItemIdAndEndBeforeAndStatus(
                userId, itemId, now, BookingStatus.APPROVED);

        if (!hasCompletedBooking) {
            throw new ValidationException("Пользователь не может оставить отзыв, так как не брал эту вещь в аренду");
        }

        // Проверяем текст комментария
        String text = commentDto.getText();
        if (text == null || text.trim().isEmpty()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }

        // Создаем комментарий
        Comment comment = ItemMapper.toComment(commentDto, author, item);
        Comment savedComment = commentRepository.save(comment);
        log.info("Комментарий успешно добавлен с ID: {}", savedComment.getId());

        return ItemMapper.toCommentDto(savedComment);
    }
}

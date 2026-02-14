package ru.practicum.shareit.item;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.shareit.booking.Booking;
import ru.practicum.shareit.booking.BookingRepository;
import ru.practicum.shareit.booking.BookingMapper;
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
import java.util.stream.Collectors;

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
        validateItem(itemDto, false);

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        Item item = ItemMapper.toItem(itemDto, owner);
        Item savedItem = itemRepository.save(item);
        return ItemMapper.toItemDto(savedItem);
    }

    @Override
    @Transactional
    public ItemDto updateItem(Long userId, Long itemId, ItemDto itemDto) {
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

        return ItemMapper.toItemDto(itemRepository.save(item));
    }

    @Override
    public ItemWithBookingsDto getItemWithBookingsById(Long itemId, Long userId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        ItemWithBookingsDto dto = toItemWithBookingsDto(item);

        // Если пользователь - владелец вещи, добавляем информацию о бронированиях
        if (item.getOwner().getId().equals(userId)) {
            addBookingInfo(dto, itemId);
        }

        // Добавляем комментарии
        addCommentsInfo(dto, itemId);

        return dto;
    }

    @Override
    public ItemDto getItemById(Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));
        return ItemMapper.toItemDto(item);
    }

    @Override
    public List<ItemWithBookingsDto> getItemsWithBookingsByOwner(Long ownerId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        List<Item> items = itemRepository.findByOwnerId(ownerId);
        List<ItemWithBookingsDto> dtos = items.stream()
                .map(this::toItemWithBookingsDto)
                .collect(Collectors.toList());

        // Добавляем информацию о бронированиях для каждой вещи
        LocalDateTime now = LocalDateTime.now();
        for (ItemWithBookingsDto dto : dtos) {
            addBookingInfo(dto, dto.getId(), now);
            addCommentsInfo(dto, dto.getId());
        }

        return dtos;
    }

    @Override
    public List<ItemDto> getItemsByOwner(Long ownerId) {
        userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        return itemRepository.findByOwnerId(ownerId).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<ItemDto> searchItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }
        return itemRepository.searchAvailableItems(text).stream()
                .map(ItemMapper::toItemDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public CommentDto addComment(Long userId, Long itemId, CreateCommentDto commentDto) {
        // Проверка текста комментария
        if (commentDto.getText() == null || commentDto.getText().isBlank()) {
            throw new ValidationException("Текст комментария не может быть пустым");
        }

        // Проверка пользователя
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));

        // Проверка вещи
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException("Вещь не найдена"));

        // Проверка, что пользователь действительно брал вещь в аренду
        boolean hasBooked = bookingRepository.hasUserBookedItem(userId, itemId, LocalDateTime.now());
        if (!hasBooked) {
            throw new ValidationException("Пользователь не брал эту вещь в аренду");
        }

        // Создание комментария
        Comment comment = Comment.builder()
                .text(commentDto.getText())
                .item(item)
                .author(author)
                .created(LocalDateTime.now())
                .build();

        Comment savedComment = commentRepository.save(comment);
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
            dto.setLastBooking(BookingMapper.toBookingDto(lastBookings.get(0)));
        }

        List<Booking> nextBookings = bookingRepository.findNextBookings(itemId, now);
        if (!nextBookings.isEmpty()) {
            dto.setNextBooking(BookingMapper.toBookingDto(nextBookings.get(0)));
        }
    }

    private void addCommentsInfo(ItemWithBookingsDto dto, Long itemId) {
        List<CommentDto> comments = commentRepository.findByItemId(itemId).stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
        dto.setComments(comments);
    }
}
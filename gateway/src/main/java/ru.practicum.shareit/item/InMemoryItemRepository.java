package ru.practicum.shareit.item;

import org.springframework.stereotype.Repository;
import ru.practicum.shareit.item.model.Item;

import java.util.*;

@Repository
public class InMemoryItemRepository {
    private final Map<Long, Item> items = new HashMap<>();
    private Long nextId = 1L;

    public Item save(Item item) {
        if (item.getId() == null) {
            item.setId(nextId++);
        }
        items.put(item.getId(), item);
        return item;
    }

    public Optional<Item> findById(Long id) {
        return Optional.ofNullable(items.get(id));
    }

    public List<Item> findByOwnerId(Long ownerId) {
        List<Item> result = new ArrayList<>();
        for (Item item : items.values()) {
            if (item.getOwner().getId().equals(ownerId)) {
                result.add(item);
            }
        }
        return result;
    }

    public List<Item> searchAvailableItems(String text) {
        if (text == null || text.isBlank()) {
            return List.of();
        }

        String lowerText = text.toLowerCase();
        List<Item> result = new ArrayList<>();
        for (Item item : items.values()) {
            if (Boolean.TRUE.equals(item.getAvailable()) &&
                    (item.getName().toLowerCase().contains(lowerText) ||
                            item.getDescription().toLowerCase().contains(lowerText))) {
                result.add(item);
            }
        }
        return result;
    }

    public boolean existsById(Long id) {
        return items.containsKey(id);
    }
}
package java.ru.practicum.shareit;

import org.junit.jupiter.api.Test;              // Добавьте этот импорт
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class ShareItTests {

    @Test                                      // Добавьте аннотацию Test
    void contextLoads() {                       // Простой тест для проверки
        // Этот тест проверяет, что контекст Spring загружается
    }
}
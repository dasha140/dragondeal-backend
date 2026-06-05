package org.example.config;

import lombok.RequiredArgsConstructor;
import org.example.domain.Category;
import org.example.domain.Game;
import org.example.repository.CategoryRepository;
import org.example.repository.GameRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DemoDataSeeder implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final GameRepository gameRepository;

    @Override
    public void run(String... args) {
        seedCategories();
        seedGames();
    }

    private void seedCategories() {
        createCategoryIfMissing("Аккаунты");
        createCategoryIfMissing("Донат");
        createCategoryIfMissing("Скины");
        createCategoryIfMissing("Бустинг");
        createCategoryIfMissing("Подписки");
        createCategoryIfMissing("Пополнение");
        createCategoryIfMissing("Предметы");
    }

    private void seedGames() {
        createGameIfMissing("Counter-Strike 2", null);
        createGameIfMissing("Dota 2", null);
        createGameIfMissing("Rust", null);
    }

    private void createCategoryIfMissing(String name) {
        boolean exists = categoryRepository.findAll()
                .stream()
                .anyMatch(category -> category.getName().equalsIgnoreCase(name));

        if (exists) {
            return;
        }

        Category category = new Category();
        category.setName(name);

        categoryRepository.save(category);
    }

    private void createGameIfMissing(String name, String imageUrl) {
        boolean exists = gameRepository.findAll()
                .stream()
                .anyMatch(game -> game.getName().equalsIgnoreCase(name));

        if (exists) {
            return;
        }

        Game game = new Game();
        game.setName(name);
        game.setImageUrl(imageUrl);

        gameRepository.save(game);
    }
}
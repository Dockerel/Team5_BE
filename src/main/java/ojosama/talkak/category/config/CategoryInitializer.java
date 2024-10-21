package ojosama.talkak.category.config;

import jakarta.annotation.PostConstruct;
import java.util.Arrays;
import ojosama.talkak.category.domain.Category;
import ojosama.talkak.category.domain.CategoryType;
import ojosama.talkak.category.repository.CategoryRepository;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Profile("local")
@Component
public class CategoryInitializer {

    private final CategoryRepository categoryRepository;

    public CategoryInitializer(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @PostConstruct
    @Transactional
    public void init() {
        Arrays.stream(CategoryType.values())
            .filter(type -> categoryRepository.findByCategoryType(type).isEmpty())
            .forEach(type -> categoryRepository.save(new Category(type)));
    }
}

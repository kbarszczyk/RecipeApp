package spring.recipeapp.converters;

import lombok.Synchronized;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import spring.recipeapp.commands.CategoryCommand;
import spring.recipeapp.domain.Category;

@Component
public class CategoryCommandToCategory implements Converter<CategoryCommand, Category> {

    @Override
    @Synchronized
    @Nullable
    public Category convert(CategoryCommand source) {
        if(source == null){
            return null;
        }
        final Category category=new Category();
        category.setId(source.getId());
        category.setDescription(source.getDescription());
        return category;
    }
}

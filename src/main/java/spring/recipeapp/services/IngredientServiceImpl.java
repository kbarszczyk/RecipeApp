package spring.recipeapp.services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import spring.recipeapp.commands.IngredientCommand;
import spring.recipeapp.converters.IngredientCommandToIngredient;
import spring.recipeapp.converters.IngredientToIngredientCommand;
import spring.recipeapp.domain.Ingredient;
import spring.recipeapp.domain.Recipe;
import spring.recipeapp.repositories.RecipeRepository;
import spring.recipeapp.repositories.UnitOfMeasureRepository;

import java.util.Optional;

@Service
public class IngredientServiceImpl implements IngredientService {

    private final IngredientToIngredientCommand ingredientToIngredientCommand;
    private final RecipeRepository recipeRepository;
    private final UnitOfMeasureRepository unitOfMeasureRepository;
    private final IngredientCommandToIngredient commandToIngredient;


    public IngredientServiceImpl(IngredientToIngredientCommand ingredientToIngredientCommand, RecipeRepository recipeRepository,
                                 UnitOfMeasureRepository unitOfMeasureRepository, IngredientCommandToIngredient commandToIngredient) {
        this.ingredientToIngredientCommand = ingredientToIngredientCommand;
        this.recipeRepository = recipeRepository;
        this.unitOfMeasureRepository = unitOfMeasureRepository;
        this.commandToIngredient = commandToIngredient;
    }

    @Override
    public IngredientCommand findByRecipeIdAndIngredientId(Long recipeId, Long ingredientId) {

        Optional<Recipe> recipeOptional=recipeRepository.findById(recipeId);
        if(recipeOptional.isEmpty()){
            throw new RuntimeException("Recipe Not Found");
        }

        Recipe recipe=recipeOptional.get();

        Optional<IngredientCommand> ingredientCommand=recipe.getIngredients().stream()
                .filter(ingredient -> ingredient.getId().equals(ingredientId))
                .map(ingredient -> ingredientToIngredientCommand.convert(ingredient)).findFirst();


        return ingredientCommand.get();
    }

    @Override
    @Transactional
    public IngredientCommand saveIngredientCommand(IngredientCommand ingredientCommand){
        Optional<Recipe> recipeOptional=recipeRepository.findById(ingredientCommand.getRecipeId());

        if(recipeOptional.isEmpty()){
            return new IngredientCommand();
        }
        else{
            Recipe recipe=recipeOptional.get();

            Optional<Ingredient> ingredientOptional=recipe
                    .getIngredients()
                    .stream()
                    .filter(ingredient -> ingredient.getId().equals(ingredientCommand.getId()))
                    .findFirst();

            if(ingredientOptional.isPresent()){
                Ingredient ingredientFound=ingredientOptional.get();
                ingredientFound.setAmount(ingredientCommand.getAmount());
                ingredientFound.setDescription(ingredientCommand.getDescription());
                ingredientFound.setUom(unitOfMeasureRepository.
                        findById(ingredientCommand.getUom().getId())
                        .orElseThrow(()->new RuntimeException("UOM Not Found!")));
            }
            else{
                recipe.addIngredient(commandToIngredient.convert(ingredientCommand));
            }

            Recipe savedRecipe=recipeRepository.save(recipe);


            return ingredientToIngredientCommand.convert(savedRecipe.getIngredients().stream()
                    .filter(ingredient -> ingredient.getId().equals(ingredientCommand.getId()))
                    .findFirst()
                    .get()
            );
        }

    }
}

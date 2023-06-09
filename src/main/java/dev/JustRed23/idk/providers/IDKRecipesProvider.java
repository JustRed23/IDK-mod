package dev.JustRed23.idk.providers;

import dev.JustRed23.idk.ModRecipeSerializers;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SpecialRecipeBuilder;

import java.util.function.Consumer;

public class IDKRecipesProvider extends RecipeProvider {

    public IDKRecipesProvider(PackOutput p_248933_) {
        super(p_248933_);
    }

    protected void buildRecipes(Consumer<FinishedRecipe> consumer) {
        SpecialRecipeBuilder.special(ModRecipeSerializers.PAINT_BUCKET_DYE.get()).save(consumer, "idk:paint_bucket_dye");
    }
}

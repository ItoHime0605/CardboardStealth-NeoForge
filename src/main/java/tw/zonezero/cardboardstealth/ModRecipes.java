package tw.zonezero.cardboardstealth;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SimpleCraftingRecipeSerializer;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Vanilla-crafting-table special recipes; the result copies the source ItemStack. */
public final class ModRecipes {
    private ModRecipes() {}
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, CardboardStealth.MOD_ID);
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DecalCraftingRecipe>> WHITE =
            SERIALIZERS.register("decal_white_carpet", () ->
                    new SimpleCraftingRecipeSerializer<>(cat -> new DecalCraftingRecipe(cat, false)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DecalCraftingRecipe>> PEEL =
            SERIALIZERS.register("decal_peel_slime", () ->
                    new SimpleCraftingRecipeSerializer<>(cat -> new DecalCraftingRecipe(cat, true)));
}

package tw.zonezero.cardboardstealth;

import net.minecraft.core.registries.Registries;
import java.util.Map;
import java.util.LinkedHashMap;
import java.util.Collections;
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
                    new SimpleCraftingRecipeSerializer<>(cat -> new DecalCraftingRecipe(cat, DecalStackRules.WHITE)));
    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DecalCraftingRecipe>> PEEL =
            SERIALIZERS.register("decal_peel_slime", () ->
                    new SimpleCraftingRecipeSerializer<>(cat -> new DecalCraftingRecipe(cat, DecalCraftingRecipe.PEEL)));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DecalCraftingRecipe>> LIGHT_GRAY =
            SERIALIZERS.register("decal_light_gray_carpet", () ->
                    new SimpleCraftingRecipeSerializer<>(cat ->
                            new DecalCraftingRecipe(cat, DecalStackRules.LIGHT_GRAY)));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DecalCraftingRecipe>> GRAY =
            SERIALIZERS.register("decal_gray_carpet", () ->
                    new SimpleCraftingRecipeSerializer<>(cat ->
                            new DecalCraftingRecipe(cat, DecalStackRules.GRAY)));

    /** Redyeing the old cardboard MUST retain decal custom data. */
    public static final Map<String, DeferredHolder<RecipeSerializer<?>,RecipeSerializer<RecolorDecalRecipe>>> RECOLOR;
    static {
        Map<String, DeferredHolder<RecipeSerializer<?>,RecipeSerializer<RecolorDecalRecipe>>> serializers =
                new LinkedHashMap<>();
        for(String name : BoxColors.NAMES) {
            serializers.put(name, SERIALIZERS.register("recolor_" + name,
                    () -> new SimpleCraftingRecipeSerializer<>(
                            category -> new RecolorDecalRecipe(category, name))));
        }
        RECOLOR = Collections.unmodifiableMap(serializers);
    }
}

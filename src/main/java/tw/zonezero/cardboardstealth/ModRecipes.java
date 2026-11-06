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

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DecalCraftingRecipe>> BLACK =
            SERIALIZERS.register("decal_black_carpet", () ->
                    new SimpleCraftingRecipeSerializer<>(cat ->
                            new DecalCraftingRecipe(cat, DecalStackRules.BLACK)));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DecalCraftingRecipe>> RED =
            SERIALIZERS.register("decal_red_carpet", () ->
                    new SimpleCraftingRecipeSerializer<>(cat ->
                            new DecalCraftingRecipe(cat, DecalStackRules.RED)));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DecalCraftingRecipe>> ORANGE =
            SERIALIZERS.register("decal_orange_carpet", () ->
                    new SimpleCraftingRecipeSerializer<>(cat ->
                            new DecalCraftingRecipe(cat, DecalStackRules.ORANGE)));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DecalCraftingRecipe>> YELLOW =
            SERIALIZERS.register("decal_yellow_carpet", () ->
                    new SimpleCraftingRecipeSerializer<>(cat ->
                            new DecalCraftingRecipe(cat, DecalStackRules.YELLOW)));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DecalCraftingRecipe>> LIME =
            SERIALIZERS.register("decal_lime_carpet", () ->
                    new SimpleCraftingRecipeSerializer<>(cat ->
                            new DecalCraftingRecipe(cat, DecalStackRules.LIME)));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DecalCraftingRecipe>> PINK =
            SERIALIZERS.register("decal_pink_carpet", () ->
                    new SimpleCraftingRecipeSerializer<>(cat ->
                            new DecalCraftingRecipe(cat, DecalStackRules.PINK)));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DecalCraftingRecipe>> BROWN =
            SERIALIZERS.register("decal_brown_carpet", () ->
                    new SimpleCraftingRecipeSerializer<>(cat ->
                            new DecalCraftingRecipe(cat, DecalStackRules.BROWN)));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DecalCraftingRecipe>> GREEN =
            SERIALIZERS.register("decal_green_carpet", () ->
                    new SimpleCraftingRecipeSerializer<>(cat ->
                            new DecalCraftingRecipe(cat, DecalStackRules.GREEN)));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DecalCraftingRecipe>> CYAN =
            SERIALIZERS.register("decal_cyan_carpet", () ->
                    new SimpleCraftingRecipeSerializer<>(cat ->
                            new DecalCraftingRecipe(cat, DecalStackRules.CYAN)));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DecalCraftingRecipe>> BLUE =
            SERIALIZERS.register("decal_blue_carpet", () ->
                    new SimpleCraftingRecipeSerializer<>(cat ->
                            new DecalCraftingRecipe(cat, DecalStackRules.BLUE)));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DecalCraftingRecipe>> PURPLE =
            SERIALIZERS.register("decal_purple_carpet", () ->
                    new SimpleCraftingRecipeSerializer<>(cat ->
                            new DecalCraftingRecipe(cat, DecalStackRules.PURPLE)));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DecalCraftingRecipe>> MAGENTA =
            SERIALIZERS.register("decal_magenta_carpet", () ->
                    new SimpleCraftingRecipeSerializer<>(cat ->
                            new DecalCraftingRecipe(cat, DecalStackRules.MAGENTA)));

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DecalCraftingRecipe>> LIGHT_BLUE =
            SERIALIZERS.register("decal_light_blue_carpet", () ->
                    new SimpleCraftingRecipeSerializer<>(cat ->
                            new DecalCraftingRecipe(cat, DecalStackRules.LIGHT_BLUE)));

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

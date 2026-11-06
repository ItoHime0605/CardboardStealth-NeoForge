package tw.zonezero.cardboardstealth;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * One paper box plus exactly one finished carpet (white/light gray/gray/black/red/orange/yellow/lime/pink/brown) or slime ball.
 * The source stack, other component data and oldest decorations survive.
 * Additional carpet families will be introduced only after their art exists.
 */
public final class DecalCraftingRecipe extends CustomRecipe {
    public static final int PEEL = -1;
    private final int color;
    private final Item ingredient;
    public DecalCraftingRecipe(CraftingBookCategory category, int color) {
        super(category);
        this.color = color;
        this.ingredient = switch (color) {
            case PEEL -> Items.SLIME_BALL;
            case DecalStackRules.WHITE -> Items.WHITE_CARPET;
            case DecalStackRules.LIGHT_GRAY -> Items.LIGHT_GRAY_CARPET;
            case DecalStackRules.GRAY -> Items.GRAY_CARPET;
            case DecalStackRules.BLACK -> Items.BLACK_CARPET;
            case DecalStackRules.RED -> Items.RED_CARPET;
            case DecalStackRules.ORANGE -> Items.ORANGE_CARPET;
            case DecalStackRules.YELLOW -> Items.YELLOW_CARPET;
            case DecalStackRules.LIME -> Items.LIME_CARPET;
            case DecalStackRules.PINK -> Items.PINK_CARPET;
            case DecalStackRules.BROWN -> Items.BROWN_CARPET;
            default -> throw new IllegalArgumentException("No finished decal for carpet: " + color);
        };
    }

    private record RecipeInputs(ItemStack box, int ingredientSlot) {}
    private RecipeInputs parse(CraftingInput input) {
        ItemStack box = ItemStack.EMPTY;
        int materialSlot = -1;
        int count = 0;
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            count++;
            if (ModItems.variant(stack.getItem()) != 0 && box.isEmpty()) {
                box = stack;
            } else if (materialSlot < 0 && stack.is(ingredient)) {
                materialSlot = i;
            } else {
                return null;
            }
        }
        if (count != 2 || box.isEmpty() || materialSlot < 0) return null;
        int existing = DecalItemData.get(box).length;
        if (color == PEEL ? existing == 0 : existing >= DecalStackRules.MAX_LAYERS) return null;
        return new RecipeInputs(box, materialSlot);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) { return parse(input) != null; }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        RecipeInputs data = parse(input);
        if (data == null) return ItemStack.EMPTY;
        int anchor = Math.floorMod(data.ingredientSlot(), DecalStackRules.ANCHORS);
        return color == PEEL ? DecalItemData.peel(data.box())
                : DecalItemData.addCarpet(data.box(), color, anchor);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return width * height >= 2; }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return switch (color) {
            case PEEL -> ModRecipes.PEEL.get();
            case DecalStackRules.WHITE -> ModRecipes.WHITE.get();
            case DecalStackRules.LIGHT_GRAY -> ModRecipes.LIGHT_GRAY.get();
            case DecalStackRules.GRAY -> ModRecipes.GRAY.get();
            case DecalStackRules.BLACK -> ModRecipes.BLACK.get();
            case DecalStackRules.RED -> ModRecipes.RED.get();
            case DecalStackRules.ORANGE -> ModRecipes.ORANGE.get();
            case DecalStackRules.YELLOW -> ModRecipes.YELLOW.get();
            case DecalStackRules.LIME -> ModRecipes.LIME.get();
            case DecalStackRules.PINK -> ModRecipes.PINK.get();
            case DecalStackRules.BROWN -> ModRecipes.BROWN.get();
            default -> throw new IllegalStateException("Missing decal serializer for " + color);
        };
    }
}

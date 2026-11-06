package tw.zonezero.cardboardstealth;

import net.minecraft.core.HolderLookup;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * One cardboard and exactly one material; no extra ingredients are accepted.
 * White carpet adds one decal layer, slime removes exactly the newest layer.
 */
public final class DecalCraftingRecipe extends CustomRecipe {
    private final boolean removal;
    public DecalCraftingRecipe(CraftingBookCategory category, boolean removal) {
        super(category);
        this.removal = removal;
    }

    private record RecipeInputs(ItemStack box, int materialSlot) {}
    private RecipeInputs parse(CraftingInput input) {
        ItemStack box = ItemStack.EMPTY;
        int materialSlot = -1;
        int count = 0;
        for (int i=0; i<input.size();i++) {
            ItemStack stack = input.getItem(i);
            if (stack.isEmpty()) continue;
            count++;
            if (ModItems.variant(stack.getItem()) != 0 && box.isEmpty()) box = stack;
            else if (materialSlot < 0 &&
                    stack.is(removal ? Items.SLIME_BALL : Items.WHITE_CARPET))
                materialSlot = i;
            else return null;
        }
        if (count != 2 || box.isEmpty() || materialSlot < 0) return null;
        int n = DecalItemData.get(box).length;
        if (removal ? n == 0 : n >= DecalStackRules.MAX_LAYERS) return null;
        return new RecipeInputs(box, materialSlot);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return parse(input) != null;
    }
    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        RecipeInputs data = parse(input);
        if (data == null) return ItemStack.EMPTY;
        // The inventory slot determines the spot on the cardboard. 2x2
        // stations offer four zones; 3x3 stations provide nine.
        int anchor = Math.floorMod(data.materialSlot(), DecalStackRules.ANCHORS);
        return removal ? DecalItemData.peel(data.box()) : DecalItemData.addWhite(data.box(), anchor);
    }
    @Override
    public boolean canCraftInDimensions(int width, int height) { return width * height >= 2; }
    @Override
    public RecipeSerializer<?> getSerializer() {
        return removal ? ModRecipes.PEEL.get() : ModRecipes.WHITE.get();
    }
}

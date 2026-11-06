package tw.zonezero.cardboardstealth;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/**
 * Same 16 vanilla dye recipes, but now copy ALL source stack component patches,
 * including the chronological 3-layer sticker list and other mod metadata.
 */
public final class RecolorDecalRecipe extends CustomRecipe {
    private final String color;
    private final Item dye;

    public RecolorDecalRecipe(CraftingBookCategory category, String color) {
        super(category);
        this.color = color;
        this.dye = BuiltInRegistries.ITEM.get(
                ResourceLocation.withDefaultNamespace(color + "_dye"));
    }

    private ItemStack source(CraftingInput input) {
        ItemStack box=ItemStack.EMPTY;
        boolean foundDye=false;
        int count=0;
        for (int i=0;i<input.size();i++) {
            ItemStack stack=input.getItem(i);
            if(stack.isEmpty())continue;
            count++;
            if(ModItems.variant(stack.getItem())!=0 && box.isEmpty())box=stack;
            else if(stack.is(dye) && !foundDye)foundDye=true;
            else return ItemStack.EMPTY;
        }
        return count==2 && foundDye ? box : ItemStack.EMPTY;
    }
    @Override
    public boolean matches(CraftingInput input, Level level) {
        return !source(input).isEmpty();
    }
    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        ItemStack old=source(input);
        if(old.isEmpty())return ItemStack.EMPTY;
        // Mojang 1.21.1: preserves CUSTOM_DATA, custom name, and any other
        // component overrides without mutating the input stack.
        return old.transmuteCopy(ModItems.DYED.get(color).get(), 1);
    }
    @Override
    public boolean canCraftInDimensions(int width,int height){return width*height>=2;}
    @Override
    public RecipeSerializer<?> getSerializer() {return ModRecipes.RECOLOR.get(color).get();}
}

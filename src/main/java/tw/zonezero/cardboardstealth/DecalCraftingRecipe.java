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
 * Printed sticker + any cardboard box adds exactly the chosen artwork.
 * The dye count was consumed when crafting the physical sticker, never
 * chosen randomly from the box variant or slot.
 *
 * Box + slime ball still peels only the latest existing layer.
 */
public final class DecalCraftingRecipe extends CustomRecipe {
    public static final int PEEL = -1;
    public static final int APPLY = 1;
    private final int operation;

    public DecalCraftingRecipe(CraftingBookCategory category, int operation) {
        super(category);
        if (operation != PEEL && operation != APPLY)
            throw new IllegalArgumentException("Unknown sticker recipe operation");
        this.operation = operation;
    }

    private record RecipeInputs(ItemStack box, int stickerSlot, DecalStickerItem sticker) {}
    private RecipeInputs parse(CraftingInput input) {
        ItemStack box = ItemStack.EMPTY;
        int stickerSlot = -1;
        DecalStickerItem printedSticker = null;
        int count = 0;
        for (int i=0;i<input.size();i++) {
            ItemStack stack=input.getItem(i);
            if(stack.isEmpty())continue;
            count++;
            if(box.isEmpty() && ModItems.variant(stack.getItem()) != 0) {
                box=stack;
            } else if(stickerSlot<0 && operation==PEEL && stack.is(Items.SLIME_BALL)) {
                stickerSlot=i;
            } else if(stickerSlot<0 && operation==APPLY &&
                    stack.getItem() instanceof DecalStickerItem sticker) {
                stickerSlot=i;
                printedSticker=sticker;
            } else return null;
        }
        if(count!=2 || box.isEmpty() || stickerSlot<0)return null;
        int existing=DecalItemData.get(box).length;
        if(operation==PEEL ? existing==0 : existing>=DecalStackRules.MAX_LAYERS)return null;
        return new RecipeInputs(box,stickerSlot,printedSticker);
    }

    @Override
    public boolean matches(CraftingInput input, Level level) { return parse(input)!=null; }

    @Override
    public ItemStack assemble(CraftingInput input, HolderLookup.Provider registries) {
        RecipeInputs data=parse(input);
        if(data==null)return ItemStack.EMPTY;
        if(operation==PEEL)return DecalItemData.peel(data.box());
        return DecalItemData.addSticker(data.box(),data.sticker().color(),
                data.sticker().artwork(),
                Math.floorMod(data.stickerSlot(), DecalStackRules.ANCHORS));
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) { return width*height>=2; }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return operation==PEEL?ModRecipes.PEEL.get():ModRecipes.APPLY.get();
    }
}

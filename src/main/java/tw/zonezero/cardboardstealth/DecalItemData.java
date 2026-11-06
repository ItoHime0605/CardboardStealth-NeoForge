package tw.zonezero.cardboardstealth;

import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

/** Retains unrelated vanilla/other-mod CUSTOM_DATA while editing one namespaced key. */
public final class DecalItemData {
    private DecalItemData() {}
    private static final String KEY = "cardboardstealth_decals_v1";

    public static int[] get(ItemStack stack) {
        if (stack.isEmpty() || ModItems.variant(stack.getItem()) == 0) return new int[0];
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new int[0] :
                DecalStackRules.sanitized(data.copyTag().getIntArray(KEY));
    }
    public static ItemStack withLayers(ItemStack stack, int[] decals) {
        ItemStack result = stack.copy();
        int[] safe = DecalStackRules.sanitized(decals);
        CustomData.update(DataComponents.CUSTOM_DATA, result, nbt -> {
            if (safe.length == 0) nbt.remove(KEY);
            else nbt.putIntArray(KEY, safe);
        });
        return result;
    }
    /**
     * Preserve the old packed integer format, only the source of color/art
     * changes. Crafted sticker is a stable choice, never a random result.
     */
    public static ItemStack addSticker(ItemStack box, int color, int artwork, int anchor) {
        int[] previous=get(box);
        if (!DecalStackRules.supportedColor(color)
                || artwork<0 || artwork>=DecalStackRules.artCount(color)
                || ModItems.variant(box.getItem())==0
                || previous.length>=DecalStackRules.MAX_LAYERS
                || anchor<0 || anchor>=DecalStackRules.ANCHORS)
            return ItemStack.EMPTY;
        int code=DecalStackRules.pack(color,artwork,anchor);
        return withLayers(box,DecalStackRules.append(previous,code));
    }

    public static ItemStack peel(ItemStack box) {
        int[] previous = get(box);
        if (previous.length == 0) return ItemStack.EMPTY;
        return withLayers(box, DecalStackRules.peel(previous));
    }
}

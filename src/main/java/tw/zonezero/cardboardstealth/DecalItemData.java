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
    public static ItemStack addWhite(ItemStack box, int carpetSlot) {
        int[] previous = get(box);
        if (ModItems.variant(box.getItem()) == 0 ||
                previous.length >= DecalStackRules.MAX_LAYERS) return ItemStack.EMPTY;
        int art = DecalStackRules.nextWhiteArt(ModItems.variant(box.getItem()),
                previous.length, carpetSlot);
        int layer = DecalStackRules.pack(0, art, carpetSlot);
        return withLayers(box, DecalStackRules.append(previous, layer));
    }
    public static ItemStack peel(ItemStack box) {
        int[] previous = get(box);
        if (previous.length == 0) return ItemStack.EMPTY;
        return withLayers(box, DecalStackRules.peel(previous));
    }
}

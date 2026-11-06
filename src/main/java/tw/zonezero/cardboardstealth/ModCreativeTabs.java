package tw.zonezero.cardboardstealth;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** One dedicated creative tab for the natural cardboard and all sixteen dyes. */
public final class ModCreativeTabs {
    private ModCreativeTabs() {}
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, CardboardStealth.MOD_ID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> TAB =
            TABS.register("cardboard_stealth", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.cardboardstealth"))
                    .icon(() -> new ItemStack(ModItems.CARDBOARD.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.CARDBOARD.get());
                        for (var dyed : ModItems.DYED.values()) output.accept(dyed.get());
                        output.accept(ModItems.BLANK_STICKER.get());
                        for (var sticker : ModItems.STICKERS.values()) output.accept(sticker.get());
                    })
                    .build());
}

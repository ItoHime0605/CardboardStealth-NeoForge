package tw.zonezero.cardboardstealth;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.block.SoundType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMachines {
    private ModMachines() {}
    public static final DeferredRegister.Blocks BLOCKS=
            DeferredRegister.createBlocks(CardboardStealth.MOD_ID);
    public static final DeferredBlock<StickerPrinterBlock> PRINTER=
            BLOCKS.registerBlock("sticker_printer", StickerPrinterBlock::new,
                    BlockBehaviour.Properties.of().mapColor(MapColor.METAL)
                            .strength(3.2F,6F).sound(SoundType.METAL).noOcclusion());

    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES=
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE,CardboardStealth.MOD_ID);
    public static final DeferredHolder<BlockEntityType<?>,BlockEntityType<StickerPrinterBlockEntity>>
            PRINTER_ENTITY=BLOCK_ENTITIES.register("sticker_printer",
                    () -> BlockEntityType.Builder.of(StickerPrinterBlockEntity::new,
                            PRINTER.get()).build(null));

    public static final DeferredRegister<MenuType<?>> MENUS=
            DeferredRegister.create(Registries.MENU,CardboardStealth.MOD_ID);
    public static final DeferredHolder<MenuType<?>,MenuType<StickerPrinterMenu>>
            PRINTER_MENU=MENUS.register("sticker_printer",
                    () -> new MenuType<>(StickerPrinterMenu::new,FeatureFlags.DEFAULT_FLAGS));
}

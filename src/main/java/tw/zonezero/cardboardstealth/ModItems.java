package tw.zonezero.cardboardstealth;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private ModItems() {}
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CardboardStealth.MOD_ID);
    public static final net.neoforged.neoforge.registries.DeferredItem<net.minecraft.world.item.BlockItem> STICKER_PRINTER =
            ITEMS.registerSimpleBlockItem(ModMachines.PRINTER, new Item.Properties());
    // Retain the original three IDs so existing worlds never lose their old boxes.
    public static final DeferredItem<Item> CARDBOARD = ITEMS.registerSimpleItem("cardboard_box", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> DELIVERY = ITEMS.registerSimpleItem("delivery_box", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> SUPPLY = ITEMS.registerSimpleItem("supply_box", new Item.Properties().stacksTo(1));
    /** Paper + slime ball creates a neutral base, independently of box dye. */
    public static final DeferredItem<Item> BLANK_STICKER =
            ITEMS.registerSimpleItem("blank_sticker", new Item.Properties().stacksTo(64));
    /** Six named, individually visible sticker items for each of the 16 colors. */
    public static final String[] STICKER_COLORS = {
            "white","light_gray","gray","black","red","orange","yellow","lime",
            "pink","brown","green","cyan","blue","purple","magenta","light_blue"
    };
    public static final Map<String, DeferredItem<DecalStickerItem>> STICKERS;
    public static final Map<String, DeferredItem<Item>> DYED;
    static {
        Map<String, DeferredItem<Item>> colors = new LinkedHashMap<>();
        for (String name : BoxColors.NAMES) {
            colors.put(name, ITEMS.registerSimpleItem(name + "_cardboard_box", new Item.Properties().stacksTo(1)));
        }
        DYED = Collections.unmodifiableMap(colors);
        Map<String, DeferredItem<DecalStickerItem>> stickers = new LinkedHashMap<>();
        for (int color=0;color<STICKER_COLORS.length;color++) {
            for (int artwork=0;artwork<6;artwork++) {
                final int savedColor=color;
                final int savedArt=artwork;
                String id="sticker_"+STICKER_COLORS[color]+"_"+(artwork+1);
                stickers.put(id, ITEMS.register(id, () ->
                        new DecalStickerItem(new Item.Properties().stacksTo(64),
                                savedColor, savedArt)));
            }
        }
        STICKERS = Collections.unmodifiableMap(stickers);
    }
    /** Wire format uses variants 1-3 for legacy, 4-19 for the new 16 dye colors. */
    public static int variant(Item item) {
        if (item == CARDBOARD.get()) return 1;
        if (item == DELIVERY.get()) return 2;
        if (item == SUPPLY.get()) return 3;
        for (int i = 0; i < BoxColors.NAMES.length; i++) {
            if (item == DYED.get(BoxColors.NAMES[i]).get()) return BoxColors.dyedVariant(i);
        }
        return 0;
    }
}

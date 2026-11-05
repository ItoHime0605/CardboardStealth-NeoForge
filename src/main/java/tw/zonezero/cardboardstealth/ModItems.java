package tw.zonezero.cardboardstealth;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    private ModItems() {}
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CardboardStealth.MOD_ID);
    // Retain the original three IDs so existing worlds never lose their old boxes.
    public static final DeferredItem<Item> CARDBOARD = ITEMS.registerSimpleItem("cardboard_box", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> DELIVERY = ITEMS.registerSimpleItem("delivery_box", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> SUPPLY = ITEMS.registerSimpleItem("supply_box", new Item.Properties().stacksTo(1));
    public static final Map<String, DeferredItem<Item>> DYED;
    static {
        Map<String, DeferredItem<Item>> colors = new LinkedHashMap<>();
        for (String name : BoxColors.NAMES) {
            colors.put(name, ITEMS.registerSimpleItem(name + "_cardboard_box", new Item.Properties().stacksTo(1)));
        }
        DYED = Collections.unmodifiableMap(colors);
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
    public static void creativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(CARDBOARD.get());
            for (var dyed : DYED.values()) event.accept(dyed.get());
            // Legacy delivery and supply items remain registered for compatibility.
        }
    }
}

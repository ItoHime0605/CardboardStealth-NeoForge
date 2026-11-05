package tw.zonezero.cardboardstealth;

import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CardboardStealth.MOD_ID);
    public static final DeferredItem<Item> CARDBOARD = ITEMS.registerSimpleItem("cardboard_box", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> DELIVERY = ITEMS.registerSimpleItem("delivery_box", new Item.Properties().stacksTo(1));
    public static final DeferredItem<Item> SUPPLY = ITEMS.registerSimpleItem("supply_box", new Item.Properties().stacksTo(1));

    public static int variant(Item item) {
        if (item == CARDBOARD.get()) return 1;
        if (item == DELIVERY.get()) return 2;
        if (item == SUPPLY.get()) return 3;
        return 0;
    }

    public static void creativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(CARDBOARD.get());
            event.accept(DELIVERY.get());
            event.accept(SUPPLY.get());
        }
    }
}

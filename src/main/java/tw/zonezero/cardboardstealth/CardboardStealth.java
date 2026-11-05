package tw.zonezero.cardboardstealth;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.NeoForge;
import tw.zonezero.cardboardstealth.client.ClientInit;

@Mod(CardboardStealth.MOD_ID)
public final class CardboardStealth {
    public static final String MOD_ID = "cardboardstealth";
    public CardboardStealth(IEventBus modBus) {
        ModItems.ITEMS.register(modBus);
        modBus.addListener(ModItems::creativeTabs);
        modBus.addListener(ModNetwork::register);
        NeoForge.EVENT_BUS.addListener(BoxEvents::playerTick);
        NeoForge.EVENT_BUS.addListener(BoxEvents::changeTarget);
        NeoForge.EVENT_BUS.addListener(BoxEvents::jump);
        NeoForge.EVENT_BUS.addListener(BoxEvents::stepSound);
        NeoForge.EVENT_BUS.addListener(BoxEvents::damaged);
        NeoForge.EVENT_BUS.addListener(BoxEvents::attack);
        NeoForge.EVENT_BUS.addListener(BoxEvents::startTracking);
        NeoForge.EVENT_BUS.addListener(BoxEvents::stopTracking);
        NeoForge.EVENT_BUS.addListener(BoxEvents::playerJoin);
        NeoForge.EVENT_BUS.addListener(BoxEvents::playerLeave);
        NeoForge.EVENT_BUS.addListener(BoxEvents::respawn);
        NeoForge.EVENT_BUS.addListener(BoxEvents::changedDimension);
        NeoForge.EVENT_BUS.addListener(BoxEvents::leftClickBlock);
        NeoForge.EVENT_BUS.addListener(BoxEvents::useItem);
        if (FMLEnvironment.dist == Dist.CLIENT) ClientInit.register(modBus);
    }
}

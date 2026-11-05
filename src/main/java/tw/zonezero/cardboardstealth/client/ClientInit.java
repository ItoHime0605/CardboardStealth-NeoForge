package tw.zonezero.cardboardstealth.client;

import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.common.NeoForge;
import tw.zonezero.cardboardstealth.CardboardStealth;

public final class ClientInit {
    public static final ModelLayerLocation BOX_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID, "cardboard"), "main");
    private static final Map<PlayerModel<?>, boolean[]> SAVED = new IdentityHashMap<>();

    public static void register(IEventBus bus) {
        bus.addListener(ClientInit::registerLayer);
        bus.addListener(ClientInit::addLayers);
        NeoForge.EVENT_BUS.addListener(ClientInit::pre);
        NeoForge.EVENT_BUS.addListener(ClientInit::post);
        NeoForge.EVENT_BUS.addListener(ClientInit::logout);
    }
    public static void registerLayer(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BOX_LAYER, CardboardLayer::createBodyLayer);
    }
    public static void addLayers(EntityRenderersEvent.AddLayers event) {
        for (var skin : event.getSkins()) {
            PlayerRenderer renderer = event.getSkin(skin);
            if (renderer != null) renderer.addLayer(new CardboardLayer(renderer, event.getEntityModels().bakeLayer(BOX_LAYER)));
        }
    }
    public static void pre(RenderPlayerEvent.Pre event) {
        if (ClientBoxState.variant(event.getEntity().getId()) == 0) return;
        PlayerModel<?> m = event.getRenderer().getModel();
        SAVED.put(m, new boolean[] {m.head.visible, m.hat.visible, m.body.visible, m.jacket.visible,
                 m.leftArm.visible, m.rightArm.visible, m.leftSleeve.visible, m.rightSleeve.visible});
        m.head.visible = m.hat.visible = m.body.visible = m.jacket.visible = false;
        m.leftArm.visible = m.rightArm.visible = m.leftSleeve.visible = m.rightSleeve.visible = false;
        // m.leftLeg and m.rightLeg intentionally stay visible.
    }
    public static void post(RenderPlayerEvent.Post event) {
        PlayerModel<?> m = event.getRenderer().getModel();
        boolean[] v = SAVED.remove(m);
        if (v == null) return;
        m.head.visible=v[0]; m.hat.visible=v[1]; m.body.visible=v[2]; m.jacket.visible=v[3];
        m.leftArm.visible=v[4]; m.rightArm.visible=v[5]; m.leftSleeve.visible=v[6]; m.rightSleeve.visible=v[7];
    }
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientBoxState.clear(); SAVED.clear();
    }
}

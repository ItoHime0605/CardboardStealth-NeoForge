package tw.zonezero.cardboardstealth.client;

import java.util.IdentityHashMap;
import java.util.Map;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import tw.zonezero.cardboardstealth.ModNetwork;
import net.neoforged.neoforge.client.event.RenderPlayerEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.neoforged.neoforge.common.util.TriState;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import tw.zonezero.cardboardstealth.BoxMovement;
import tw.zonezero.cardboardstealth.BoxSoundRules;
import net.neoforged.neoforge.common.NeoForge;
import tw.zonezero.cardboardstealth.CardboardStealth;

public final class ClientInit {
    public static final ModelLayerLocation BOX_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID, "cardboard"), "main");
    private static final Map<PlayerModel<?>, boolean[]> SAVED = new IdentityHashMap<>();

    public static void register(IEventBus bus) {
        bus.addListener(ClientInit::registerLayer);
        bus.addListener(ClientInit::addLayers);
        bus.addListener(BoxViewOverlay::register);
        NeoForge.EVENT_BUS.addListener(ClientInit::pre);
        NeoForge.EVENT_BUS.addListener(ClientInit::post);
        NeoForge.EVENT_BUS.addListener(ClientInit::nameplate);
        NeoForge.EVENT_BUS.addListener(ClientInit::logout);
        NeoForge.EVENT_BUS.addListener(ClientInit::jump);
        NeoForge.EVENT_BUS.addListener(ClientInit::clientSounds);
        NeoForge.EVENT_BUS.addListener(ClientInit::emptySwing);
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
        PlayerModel<?> m = event.getRenderer().getModel();
        // Defensive restore if an earlier render was interrupted by another mod.
        boolean[] stale = SAVED.remove(m);
        if (stale != null) restore(m, stale);
        if (ClientBoxState.variant(event.getEntity().getId()) == 0) return;
        SAVED.put(m, new boolean[] {m.head.visible, m.hat.visible, m.body.visible, m.jacket.visible,
                 m.leftArm.visible, m.rightArm.visible, m.leftSleeve.visible, m.rightSleeve.visible});
        m.head.visible = m.hat.visible = m.body.visible = m.jacket.visible = false;
        m.leftArm.visible = m.rightArm.visible = m.leftSleeve.visible = m.rightSleeve.visible = false;
        // m.leftLeg and m.rightLeg intentionally stay visible.
    }
    public static void post(RenderPlayerEvent.Post event) {
        PlayerModel<?> m = event.getRenderer().getModel();
        boolean[] v = SAVED.remove(m);
        if (v != null) restore(m, v);
    }
    private static void restore(PlayerModel<?> m, boolean[] v) {
        m.head.visible=v[0]; m.hat.visible=v[1]; m.body.visible=v[2]; m.jacket.visible=v[3];
        m.leftArm.visible=v[4]; m.rightArm.visible=v[5]; m.leftSleeve.visible=v[6]; m.rightSleeve.visible=v[7];
    }
    /** Boxed players must appear as scenery, not as tagged players. */
    public static void nameplate(RenderNameTagEvent event) {
        if (event.getEntity() instanceof Player player &&
                !tw.zonezero.cardboardstealth.BoxRenderRules.showPlayerNameplate(
                        ClientBoxState.variant(player.getId()) != 0)) {
            event.setCanRender(TriState.FALSE);
        }
    }
    // Apply the same half-block jump impulse on the predicting client and the server.
    // Entity IDs are synced by ModNetwork; do not rely on server-only persistent NBT here.
    public static void jump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity() instanceof LocalPlayer player && ClientBoxState.variant(player.getId()) != 0) {
            BoxMovement.limitJump(player);
        }
    }
    // Server-side persistent NBT is NOT synchronized to the client, so the
    // audible local sound must use the explicit BoxPayload state instead.
    public static void clientSounds(PlayLevelSoundEvent.AtEntity event) {
        if (!(event.getEntity() instanceof Player player) || !player.level().isClientSide()
                || ClientBoxState.variant(player.getId()) == 0 || event.getSound() == null) return;
        String path = event.getSound().value().getLocation().getPath();
        if (BoxSoundRules.isStep(path)) event.setNewVolume(event.getNewVolume() * 0.18F);
        else if (BoxSoundRules.isLanding(path))
            event.setNewVolume(event.getNewVolume() * 0.28F);
    }
    public static void emptySwing(PlayerInteractEvent.LeftClickEmpty event) {
        if (event.getEntity() instanceof LocalPlayer player &&
                ClientBoxState.variant(player.getId()) != 0) {
            PacketDistributor.sendToServer(new ModNetwork.MeleePayload());
        }
    }
    public static void logout(ClientPlayerNetworkEvent.LoggingOut event) {
        ClientBoxState.clear(); SAVED.clear();
    }
}

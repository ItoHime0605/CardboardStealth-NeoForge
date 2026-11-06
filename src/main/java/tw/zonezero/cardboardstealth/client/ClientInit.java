package tw.zonezero.cardboardstealth.client;

import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.event.RegisterItemDecorationsEvent;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import tw.zonezero.cardboardstealth.DecalItemData;
import tw.zonezero.cardboardstealth.DecalStackRules;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import tw.zonezero.cardboardstealth.ModNetwork;
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
import tw.zonezero.cardboardstealth.ModItems;
import tw.zonezero.cardboardstealth.ModMachines;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

public final class ClientInit {
    public static final ModelLayerLocation BOX_LAYER = new ModelLayerLocation(
            ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID, "cardboard"), "main");

    public static void register(IEventBus bus) {
        bus.addListener(ClientInit::registerLayer);
        bus.addListener(ClientInit::registerPrinterScreen);
        bus.addListener(ClientInit::addLayers);
        bus.addListener(ClientInit::registerDecalItemIcons);
        bus.addListener(BoxViewOverlay::register);
        NeoForge.EVENT_BUS.addListener(ClientInit::nameplate);
        NeoForge.EVENT_BUS.addListener(ClientInit::logout);
        NeoForge.EVENT_BUS.addListener(ClientInit::jump);
        NeoForge.EVENT_BUS.addListener(ClientInit::clientSounds);
        NeoForge.EVENT_BUS.addListener(ClientInit::emptySwing);
        NeoForge.EVENT_BUS.addListener(ClientInit::itemTooltip);
    }
    /**
     * Inventories are 2D by design. Show the small paper-art thumbnail on the
     * icon for each actual layer (oldest first). These are the same 6 images
     * rendered at full resolution on the player's worn 3D cardboard.
     */
    public static void registerDecalItemIcons(RegisterItemDecorationsEvent event) {
        net.neoforged.neoforge.client.IItemDecorator decorator = (graphics, font, stack, x, y) -> {
            int[] decals = DecalItemData.get(stack);
            for (int i=0;i<decals.length;i++) {
                int code=decals[i];
                ResourceLocation atlas = CardboardDecalRenderer.atlasFor(DecalStackRules.color(code));
                if (atlas == null) continue;
                int art=DecalStackRules.art(code);
                // Minecraft GUI rendering crops the specified 128x80 pixel
                // portion of the 256x240 texture to a readable 10x6 stamp.
                graphics.blit(atlas,
                        x + 2 + 2*i, y + 7 - i, 10, 6,
                        (art%2)*128F, (art/2)*80F,
                        128, 80, 256, 240);
            }
            return false;
        };
        event.register(ModItems.CARDBOARD.get(), decorator);
        event.register(ModItems.DELIVERY.get(), decorator);
        event.register(ModItems.SUPPLY.get(), decorator);
        for (var item : ModItems.DYED.values()) event.register(item.get(), decorator);
    }
    public static void itemTooltip(ItemTooltipEvent event) {
        // Printed sticker is a physical item: show its exact numbered design
        // before crafting, rather than a randomly chosen decal afterwards.
        if(event.getItemStack().getItem() instanceof tw.zonezero.cardboardstealth.DecalStickerItem sticker) {
            String key=ModItems.STICKER_COLORS[sticker.color()]+"_"+(sticker.artwork()+1);
            event.getToolTip().add(Component.translatable(
                    "tooltip.cardboardstealth.sticker_pattern",
                    Component.translatable("item.cardboardstealth.sticker_subject_"+key)));
            event.getToolTip().add(Component.translatable(
                    "tooltip.cardboardstealth.sticker_dyes",sticker.artwork()+1));
            event.getToolTip().add(Component.translatable(
                    "tooltip.cardboardstealth.sticker_use"));
            return;
        }
        if(event.getItemStack().is(ModItems.BLANK_STICKER.get())) {
            event.getToolTip().add(Component.translatable("tooltip.cardboardstealth.blank_sticker"));
            return;
        }
        int[] layers=DecalItemData.get(event.getItemStack());
        if (layers.length == 0) return;
        event.getToolTip().add(Component.translatable("tooltip.cardboardstealth.decals",
                layers.length, DecalStackRules.MAX_LAYERS));
        for (int i=0;i<layers.length;i++) {
            int code=layers[i];
            String group = switch (DecalStackRules.color(code)) {
                case DecalStackRules.WHITE -> "white_";
                case DecalStackRules.LIGHT_GRAY -> "light_gray_";
                case DecalStackRules.GRAY -> "gray_";
                case DecalStackRules.BLACK -> "black_";
                case DecalStackRules.RED -> "red_";
                case DecalStackRules.ORANGE -> "orange_";
                case DecalStackRules.YELLOW -> "yellow_";
                case DecalStackRules.LIME -> "lime_";
                case DecalStackRules.PINK -> "pink_";
                case DecalStackRules.BROWN -> "brown_";
                case DecalStackRules.GREEN -> "green_";
                case DecalStackRules.CYAN -> "cyan_";
                case DecalStackRules.BLUE -> "blue_";
                case DecalStackRules.PURPLE -> "purple_";
                case DecalStackRules.MAGENTA -> "magenta_";
                case DecalStackRules.LIGHT_BLUE -> "light_blue_";
                default -> null;
            };
            if (group != null) event.getToolTip().add(Component.translatable(
                    "tooltip.cardboardstealth." + group + DecalStackRules.art(code),
                    i + 1));
        }
    }
    public static void registerPrinterScreen(RegisterMenuScreensEvent event) {
        event.register(ModMachines.PRINTER_MENU.get(), StickerPrinterScreen::new);
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
    /**
     * Do not mutate PlayerRenderer attached layers or shared PlayerModel part
     * visibility: other mods own their own render layers. When any player
     * activates a disguise, only our separately registered CardboardLayer
     * uses the synchronized visual state. No Pre/Post restoration is required.
     * This avoids a client-wide leak after canceled player-render events.
     */
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
        ClientBoxState.clear();
    }
}

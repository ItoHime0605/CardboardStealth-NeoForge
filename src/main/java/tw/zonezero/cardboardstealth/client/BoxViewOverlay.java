package tw.zonezero.cardboardstealth.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import tw.zonezero.cardboardstealth.CardboardStealth;

/** Temporary inside-the-box camera mask, NOT the final cardboard model or texture. */
public final class BoxViewOverlay {
    private static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID, "box_eye_slit");
    private static final int INTERIOR = 0xFF21160F;

    private BoxViewOverlay() {}

    public static void register(RegisterGuiLayersEvent event) {
        // HUD stays legible: the mask draws before the hotbar, health, and other HUD layers.
        event.registerBelow(VanillaGuiLayers.HOTBAR, ID, (graphics, delta) -> draw(graphics));
    }

    private static void draw(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.options.getCameraType().isFirstPerson()
                || mc.screen != null || ClientBoxState.variant(mc.player.getId()) == 0) return;
        int w = graphics.guiWidth();
        int h = graphics.guiHeight();
        // A narrow eye slit across the middle of the screen. World remains visible only here.
        int slitHeight = Math.max(6, h / 16);
        int slitWidth = (int) (w * 0.70F);
        int x1 = (w - slitWidth) / 2;
        int x2 = x1 + slitWidth;
        int y1 = (h - slitHeight) / 2;
        int y2 = y1 + slitHeight;
        graphics.fill(0, 0, w, y1, INTERIOR);
        graphics.fill(0, y2, w, h, INTERIOR);
        graphics.fill(0, y1, x1, y2, INTERIOR);
        graphics.fill(x2, y1, w, y2, INTERIOR);
    }
}

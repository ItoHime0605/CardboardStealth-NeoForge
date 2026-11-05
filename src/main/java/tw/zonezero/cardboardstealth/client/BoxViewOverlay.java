package tw.zonezero.cardboardstealth.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import tw.zonezero.cardboardstealth.BoxViewGeometry;
import tw.zonezero.cardboardstealth.CardboardStealth;

/**
 * Client-only cardboard interior mask. This is a placeholder before the user
 * supplies final box texture/model references, not a design of the outside box.
 */
public final class BoxViewOverlay {
    private static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID, "box_eye_slit");
    // Sampled from the user's 757x496 example of the inside-the-box view.
    private static final int INTERIOR = 0xFFA26B4D;

    private BoxViewOverlay() {}

    public static void register(RegisterGuiLayersEvent event) {
        // Keep crosshair, health, hotbar and TaCZ HUD usable above the cardboard mask.
        event.registerBelow(VanillaGuiLayers.HOTBAR, ID, (graphics, delta) -> draw(graphics));
    }

    private static void draw(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.options.getCameraType().isFirstPerson()
                || mc.screen != null || ClientBoxState.variant(mc.player.getId()) == 0) return;

        int w = graphics.guiWidth();
        int h = graphics.guiHeight();
        BoxViewGeometry.Aperture hole = BoxViewGeometry.calculate(w, h);

        // Everything outside the rounded rectangular window is opaque cardboard.
        graphics.fill(0, 0, w, hole.top(), INTERIOR);
        graphics.fill(0, hole.bottom(), w, h, INTERIOR);
        // Draw the straight sides in one batch for performance.
        int straightTop = hole.top() + hole.radiusY();
        int straightBottom = hole.bottom() - hole.radiusY();
        if (straightBottom > straightTop) {
            graphics.fill(0, straightTop, hole.left(), straightBottom, INTERIOR);
            graphics.fill(hole.right(), straightTop, w, straightBottom, INTERIOR);
        }

        // Elliptical rounded corners: at most 2 * radiusY scanlines (about 56 at 1080p).
        // The world and first-person gun are still rendered by Minecraft as normal,
        // but only the view-window region remains visible through this GUI mask.
        for (int y = hole.top(); y < hole.bottom(); y++) {
            if (y >= straightTop && y < straightBottom) continue;
            int inset = hole.insetAt(y);
            graphics.fill(0, y, hole.left() + inset, y + 1, INTERIOR);
            graphics.fill(hole.right() - inset, y, w, y + 1, INTERIOR);
        }
    }
}

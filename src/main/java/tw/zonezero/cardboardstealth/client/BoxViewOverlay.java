package tw.zonezero.cardboardstealth.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import tw.zonezero.cardboardstealth.BoxInteriorPalette;
import tw.zonezero.cardboardstealth.BoxViewGeometry;
import tw.zonezero.cardboardstealth.CardboardStealth;

/**
 * A warm, shadowed first-person cardboard interior (color matched to box dye).
 * The large 45% x 31% rounded eye window is a player-only UI illusion and is
 * intentionally much larger than the slender physical viewing slit outside.
 */
public final class BoxViewOverlay {
    private static final ResourceLocation ID =
            ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID, "box_eye_slit");
    private BoxViewOverlay() {}

    public static void register(RegisterGuiLayersEvent event) {
        event.registerBelow(VanillaGuiLayers.HOTBAR, ID, (graphics, delta) -> draw(graphics));
    }

    private static void draw(GuiGraphics graphics) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || !mc.options.getCameraType().isFirstPerson() || mc.screen != null) return;
        int variant = ClientBoxState.variant(mc.player.getId());
        if (variant == 0) return;
        int w = graphics.guiWidth(), h = graphics.guiHeight();
        BoxViewGeometry.Aperture aperture = BoxViewGeometry.calculate(w, h);
        int paper = BoxInteriorPalette.innerBase(variant);
        int top = aperture.top(), bottom = aperture.bottom();
        int topQuarter = Math.max(0, top / 4);
        int topTwoThirds = Math.max(topQuarter, (top * 2) / 3);

        // Recessed roof panels, separated by natural fold shading.
        graphics.fill(0, 0, w, topQuarter, BoxInteriorPalette.shade(paper, 0.73F));
        graphics.fill(0, topQuarter, w, topTwoThirds, BoxInteriorPalette.shade(paper, 0.87F));
        graphics.fill(0, topTwoThirds, w, top, paper);

        int footShade = Math.min(h, bottom + Math.max(1, (h - bottom) / 3));
        graphics.fill(0, bottom, w, footShade, BoxInteriorPalette.shade(paper, 0.88F));
        graphics.fill(0, footShade, w, h, BoxInteriorPalette.shade(paper, 0.69F));

        int cornerTop = top + aperture.radiusY();
        int cornerBottom = bottom - aperture.radiusY();
        // Left and right cardboard walls get different light levels.
        graphics.fill(0, cornerTop, aperture.left(), cornerBottom,
                BoxInteriorPalette.shade(paper, 0.91F));
        graphics.fill(aperture.right(), cornerTop, w, cornerBottom,
                BoxInteriorPalette.shade(paper, 0.79F));

        int lip = Math.max(2, w / 180);
        graphics.fill(Math.max(0, aperture.left() - lip), cornerTop,
                aperture.left(), cornerBottom, BoxInteriorPalette.shade(paper, 0.57F));
        graphics.fill(aperture.right(), cornerTop, Math.min(w, aperture.right() + lip),
                cornerBottom, BoxInteriorPalette.shade(paper, 0.55F));

        // Rounded corner rows. The world and first-person pistol remain visible
        // inside the opening; nothing is drawn over that area.
        for (int y=top; y<bottom; y++) {
            if (y >= cornerTop && y < cornerBottom) continue;
            int x = aperture.insetAt(y);
            int color = BoxInteriorPalette.shade(paper, y < cornerTop ? 0.92F : 0.79F);
            graphics.fill(0, y, aperture.left() + x, y + 1, color);
            graphics.fill(aperture.right() - x, y, w, y + 1, color);
            if (x > 0) {
                int innerRim = BoxInteriorPalette.shade(paper, 0.57F);
                graphics.fill(aperture.left() + x - 1, y, aperture.left() + x, y + 1, innerRim);
                graphics.fill(aperture.right() - x, y, aperture.right() - x + 1, y + 1, innerRim);
            }
        }
        // Sparse deterministic 1px paper-fibre streaks (no random flicker).
        int fibre = BoxInteriorPalette.shade(paper, 0.91F);
        for (int i = 0; i < 48; i++) {
            int x = Math.floorMod(173 * i + 47, Math.max(1, w));
            int y = Math.floorMod(139 * i + 13, Math.max(1, h));
            if (withinWindow(aperture, x, y)) continue;
            graphics.fill(x, y, Math.min(w, x + 2 + i % 4), Math.min(h, y + 1), fibre);
        }
        int fold = BoxInteriorPalette.shade(paper, 0.69F);
        graphics.fill(w / 6, 0, w / 6 + 1, Math.max(0, top - 5), fold);
        graphics.fill(5 * w / 6, 0, 5 * w / 6 + 1, Math.max(0, top - 5), fold);
        graphics.fill(w / 10, bottom + 3, w / 10 + 1, h, fold);
        graphics.fill(9 * w / 10, bottom + 3, 9 * w / 10 + 1, h, fold);
    }

    private static boolean withinWindow(BoxViewGeometry.Aperture a, int x, int y) {
        return y >= a.top() && y < a.bottom()
                && x >= a.left() + a.insetAt(y) && x < a.right() - a.insetAt(y);
    }
}

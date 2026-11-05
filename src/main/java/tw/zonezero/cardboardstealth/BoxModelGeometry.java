package tw.zonezero.cardboardstealth;

/**
 * Authoritative vanilla-model-pixel dimensions for the actual third-person box.
 * The wider GUI aperture is intentionally independent of the exterior peep slot.
 */
public final class BoxModelGeometry {
    private BoxModelGeometry() {}
    public static final float WIDTH_PIXELS = 18.0F;
    public static final float DEPTH_PIXELS = 22.0F;
    public static final float HEIGHT_PIXELS = 21.0F;
    public static final float TOP_Y = -5.0F;
    public static final float FLOOR_BODY_Y = 16.0F;
    public static final float FRONT_Z = -11.0F;
    public static final float BACK_Z = 11.0F;
    // A discreet exterior peep slit (NOT the larger interior player HUD window).
    public static final float SLIT_HALF_WIDTH = 5.25F;
    public static final float SLIT_TOP_Y = -0.80F;
    public static final float SLIT_BOTTOM_Y = 0.70F;
    public static final float PANEL_THICKNESS = 0.72F;

    public static float slitWidth() { return SLIT_HALF_WIDTH * 2.0F; }
    public static float slitHeight() { return SLIT_BOTTOM_Y - SLIT_TOP_Y; }
    public static float externalSlitAreaRatio() {
        return (slitWidth() * slitHeight()) / (WIDTH_PIXELS * HEIGHT_PIXELS);
    }

    /** Separate shading preserves the original dye hue while letting folded edges read as cardboard. */
    public static int multiplyTint(int rgb, float factor) {
        int r = Math.max(0, Math.min(255, Math.round(((rgb >> 16) & 255) * factor)));
        int g = Math.max(0, Math.min(255, Math.round(((rgb >> 8) & 255) * factor)));
        int b = Math.max(0, Math.min(255, Math.round((rgb & 255) * factor)));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}

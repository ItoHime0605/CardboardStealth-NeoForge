package tw.zonezero.cardboardstealth;

/** Matte first-person paper interiors, tinted to the equipped box's dye. */
public final class BoxInteriorPalette {
    private BoxInteriorPalette() {}
    private static final int WARM_PAPER = 0xB4A08C;
    public static int innerBase(int variant) {
        return shade(mix(BoxColors.rgb(variant), WARM_PAPER, 0.20F), 0.58F);
    }
    public static int mix(int a, int b, float portionB) {
        float t = Math.max(0F, Math.min(1F, portionB));
        int r = Math.round(((a >> 16) & 255) * (1F - t) + ((b >> 16) & 255) * t);
        int g = Math.round(((a >> 8) & 255) * (1F - t) + ((b >> 8) & 255) * t);
        int bl = Math.round((a & 255) * (1F - t) + (b & 255) * t);
        return (r << 16) | (g << 8) | bl;
    }
    public static int shade(int rgb, float factor) {
        int r = clamp(Math.round(((rgb >> 16) & 255) * factor));
        int g = clamp(Math.round(((rgb >> 8) & 255) * factor));
        int b = clamp(Math.round((rgb & 255) * factor));
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
    private static int clamp(int x) { return Math.min(255, Math.max(0, x)); }
}

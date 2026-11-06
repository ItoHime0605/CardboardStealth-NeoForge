package tw.zonezero.cardboardstealth;

import java.util.Arrays;

/**
 * Small, deterministic encoding for up to three independent cardboard decals.
 * Designed to remain stable when more carpet colors receive their artwork.
 *
 * Each positive integer stores one layer: 1+((color<<12)|(art<<8)|anchor).
 * The array order is the actual paint order; the last layer is on top.
 */
public final class DecalStackRules {
    private DecalStackRules() {}
    public static final int MAX_LAYERS = 3;
    public static final int MAX_COLORS = 16;
    public static final int WHITE_ART_COUNT = 6;
    public static final int ANCHORS = 9;

    public static int pack(int color, int art, int anchor) {
        if (color < 0 || color >= MAX_COLORS || art < 0 || art >= 16 || anchor < 0 || anchor >= ANCHORS)
            throw new IllegalArgumentException("Invalid cardboard decal");
        return 1 + (color << 12) + (art << 8) + anchor;
    }
    public static int color(int value) { return ((value - 1) >>> 12) & 15; }
    public static int art(int value) { return ((value - 1) >>> 8) & 15; }
    public static int anchor(int value) { return (value - 1) & 255; }
    public static boolean isValid(int value) {
        return value > 0 && color(value) < MAX_COLORS && anchor(value) < ANCHORS &&
                (color(value) != 0 || art(value) < WHITE_ART_COUNT);
    }
    public static int[] sanitized(int[] values) {
        if (values == null) return new int[0];
        int[] out = new int[Math.min(MAX_LAYERS, values.length)];
        int length = 0;
        for (int value : values) {
            if (isValid(value) && length < MAX_LAYERS) out[length++] = value;
        }
        return Arrays.copyOf(out, length);
    }
    public static int[] append(int[] old, int value) {
        int[] layers = sanitized(old);
        if (!isValid(value) || layers.length >= MAX_LAYERS) return layers;
        int[] next = Arrays.copyOf(layers, layers.length + 1);
        next[next.length - 1] = value;
        return next;
    }
    public static int[] peel(int[] old) {
        int[] layers = sanitized(old);
        return layers.length == 0 ? layers : Arrays.copyOf(layers, layers.length - 1);
    }
    /** Each white carpet stamp is chosen reproducibly, without server RNG. */
    public static int nextWhiteArt(int originalVariant, int priorCount, int placement) {
        return Math.floorMod(originalVariant + 2 * priorCount + (placement / 3), WHITE_ART_COUNT);
    }
}

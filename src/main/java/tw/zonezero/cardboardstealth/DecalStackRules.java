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
    public static final int LIGHT_GRAY_ART_COUNT = 6;
    public static final int GRAY_ART_COUNT = 6;
    public static final int BLACK_ART_COUNT = 6;
    public static final int RED_ART_COUNT = 6;
    public static final int ORANGE_ART_COUNT = 6;
    public static final int YELLOW_ART_COUNT = 6;
    public static final int LIME_ART_COUNT = 6;
    public static final int PINK_ART_COUNT = 6;
    public static final int BROWN_ART_COUNT = 6;
    public static final int GREEN_ART_COUNT = 6;
    public static final int CYAN_ART_COUNT = 6;
    public static final int BLUE_ART_COUNT = 6;
    public static final int WHITE = 0;
    public static final int LIGHT_GRAY = 1;
    public static final int GRAY = 2;
    public static final int BLACK = 3;
    public static final int RED = 4;
    public static final int ORANGE = 5;
    public static final int YELLOW = 6;
    public static final int LIME = 7;
    public static final int PINK = 8;
    public static final int BROWN = 9;
    public static final int GREEN = 10;
    public static final int CYAN = 11;
    public static final int BLUE = 12;
    /** Only finished artwork families may be added to a box. */
    public static boolean supportedColor(int color) {
        return color == WHITE || color == LIGHT_GRAY || color == GRAY || color == BLACK || color == RED || color == ORANGE || color == YELLOW || color == LIME || color == PINK || color == BROWN || color == GREEN || color == CYAN || color == BLUE;
    }
    public static int artCount(int color) {
        return switch (color) {
            case WHITE -> WHITE_ART_COUNT;
            case LIGHT_GRAY -> LIGHT_GRAY_ART_COUNT;
            case GRAY -> GRAY_ART_COUNT;
            case BLACK -> BLACK_ART_COUNT;
            case RED -> RED_ART_COUNT;
            case ORANGE -> ORANGE_ART_COUNT;
            case YELLOW -> YELLOW_ART_COUNT;
            case LIME -> LIME_ART_COUNT;
            case PINK -> PINK_ART_COUNT;
            case BROWN -> BROWN_ART_COUNT;
            case GREEN -> GREEN_ART_COUNT;
            case CYAN -> CYAN_ART_COUNT;
            case BLUE -> BLUE_ART_COUNT;
            default -> 0;
        };
    }
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
        return value > 0 && supportedColor(color(value)) &&
                art(value) < artCount(color(value)) && anchor(value) < ANCHORS;
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
    /** Different art per carpet family; deterministic and stable across restarts. */
    public static int nextArt(int color, int originalVariant, int priorCount, int placement) {
        int size = artCount(color);
        if (size == 0) throw new IllegalArgumentException("Carpet artwork is not implemented");
        return Math.floorMod(originalVariant + 2 * priorCount + (placement / 3)
                + color * 3 + placement % 3, size);
    }
    /** Compatible with stacks crafted before the light-gray art expansion. */
    public static int nextWhiteArt(int originalVariant, int priorCount, int placement) {
        return Math.floorMod(originalVariant + 2 * priorCount + (placement / 3), WHITE_ART_COUNT);
    }
}

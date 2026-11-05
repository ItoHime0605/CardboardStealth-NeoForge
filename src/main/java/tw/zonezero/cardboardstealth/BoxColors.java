package tw.zonezero.cardboardstealth;

/** One shared color palette for item variants, client item tints and worn box models. */
public final class BoxColors {
    private BoxColors() {}
    public static final int FIRST_DYED_VARIANT = 4;
    public static final String[] NAMES = {
        "white", "orange", "magenta", "light_blue", "yellow", "lime",
        "pink", "gray", "light_gray", "cyan", "purple", "blue",
        "brown", "green", "red", "black"
    };
    private static final int[] COLORS = {
        0xE9ECEF, 0xEA8524, 0xBE49C9, 0x62B9DE, 0xEBC62D, 0x79BF39,
        0xE9A0B0, 0x53565C, 0xA7A9A9, 0x258F95, 0x8139AA, 0x344D9F,
        0x81502C, 0x517A35, 0xA53A35, 0x27272D
    };

    public static int dyedVariant(int colorIndex) {
        if (colorIndex < 0 || colorIndex >= NAMES.length) return 0;
        return FIRST_DYED_VARIANT + colorIndex;
    }
    public static String colorName(int variant) {
        int index = variant - FIRST_DYED_VARIANT;
        return index >= 0 && index < NAMES.length ? NAMES[index] : "";
    }
    public static int rgb(int variant) {
        return switch (variant) {
            case 1 -> 0xCF9B66; // Existing un-dyed legacy cardboard
            case 2 -> 0xAD4A40; // Existing legacy delivery box
            case 3 -> 0x608044; // Existing legacy supply box
            default -> {
                int index = variant - FIRST_DYED_VARIANT;
                yield index >= 0 && index < COLORS.length ? COLORS[index] : 0xFFFFFF;
            }
        };
    }
}

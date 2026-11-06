package tw.zonezero.cardboardstealth;

/**
 * Small deterministic, Minecraft-independent printer validation.
 * The final dye color comes only from the inserted dye item, never client input.
 */
public final class StickerPrinterRules {
    private StickerPrinterRules() {}
    public static final int COLORS=16;
    public static final int PATTERNS=6;
    public static int requiredDye(int pattern) {
        return pattern >= 0 && pattern < PATTERNS ? pattern+1 : 0;
    }
    public static boolean canPrint(int blankCount,int dyeCount,int color,int pattern,
                                   boolean outputEmpty) {
        return blankCount>0 && color>=0 && color<COLORS &&
                requiredDye(pattern)>0 && dyeCount>=requiredDye(pattern) && outputEmpty;
    }
    public static String outputId(String[] colors,int color,int pattern) {
        if (colors==null||colors.length!=COLORS||color<0||color>=COLORS
                ||requiredDye(pattern)==0)throw new IllegalArgumentException("Printer design");
        return "sticker_"+colors[color]+"_"+(pattern+1);
    }
}

package tw.zonezero.cardboardstealth;

import java.util.Locale;

/** Data-only weapon and sound rules, covered by Java-only CI tests. */
public final class BoxPolicy {
    private BoxPolicy() {}

    /**
     * Read TaCZ's actual gun index 'type', not a guessed gun ID/display name.
     * Unknown categories intentionally do NOT qualify as pistols.
     */
    public static boolean isPistolCategory(String category) {
        if (category == null) return false;
        return switch (category.trim().toLowerCase(Locale.ROOT)) {
            case "pistol", "handgun", "revolver" -> true;
            default -> false;
        };
    }

    /** Suppressors reduce the ZA sound trace, but a fired shot is never silent. */
    public static int gunshotSenseStrength(int configuredStrength, boolean suppressed) {
        int strength = Math.max(1, configuredStrength);
        return suppressed ? Math.max(1, strength / 4) : strength;
    }
}

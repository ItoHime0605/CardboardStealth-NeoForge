package tw.zonezero.cardboardstealth;

/**
 * Non-invasive player rendering policy. Our own cardboard overlay draws on top
 * of vanilla player rendering, but MUST NOT remove or mutate other installed
 * mods' player layers, armor, Curios, TaCZ or skin visibility flags.
 *
 * Opaque cardboard geometry hides most skin while equipped; some external
 * attachments may remain visible if they extend outside the physical box.
 * This is deliberate: preserving players' rendering integrity takes priority
 * over a visually perfect illusion. No global shared-renderer mutation.
 */
public final class BoxRenderRules {
    private BoxRenderRules() {}

    public static boolean showPlayerNameplate(boolean disguised) {
        return !disguised;
    }

    /** Compatibility fallback: third-party attachment layers are always owned by their mods. */
    public static boolean renderAttachedLayer(boolean disguised, boolean cardboardLayer) {
        return true;
    }
}

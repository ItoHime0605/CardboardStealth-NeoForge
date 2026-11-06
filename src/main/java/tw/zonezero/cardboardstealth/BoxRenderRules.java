package tw.zonezero.cardboardstealth;

/**
 * Stateless, per-draw-call player concealment policy.
 *
 * NEVER hide ModelPart.visible, edit renderer.layers, alter shader color or
 * record a player-specific global "currently rendering" flag. Suppress only
 * the exact draw operation when THIS player is boxed. The next invocation
 * re-evaluates the player's current state, so there is nothing to restore.
 */
public final class BoxRenderRules {
    private BoxRenderRules() {}

    /** Vanilla player SKIN renders normally as soon as the cardboard is gone. */
    public static boolean renderBody(boolean disguised) {
        return !disguised;
    }

    /** All external layers resume automatically; the cardboard alone remains. */
    public static boolean renderAttachedLayer(boolean disguised, boolean cardboardLayer) {
        return !disguised || cardboardLayer;
    }

    public static boolean showPlayerNameplate(boolean disguised) {
        return !disguised;
    }
}

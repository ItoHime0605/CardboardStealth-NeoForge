package tw.zonezero.cardboardstealth;

/**
 * A cardboard disguise is opaque to outside viewers. In third-person,
 * only the box model may render from the player's attached visual layers:
 * hands, weapons, armor, capes, wings and other accessories stay inside.
 * This affects rendering only; equipment and combat attributes are unchanged.
 */
public final class BoxRenderRules {
    private BoxRenderRules() {}

    public static boolean renderAttachedLayer(boolean disguised, boolean cardboardLayer) {
        return !disguised || cardboardLayer;
    }
}

package tw.zonezero.cardboardstealth;

/**
 * Pure, side-neutral cardboard activation rules. This class intentionally has no
 * dependency on the Minecraft engine so regressions can be caught by the CI JVM.
 */
public final class BoxActivationRules {
    private BoxActivationRules() {}
    public static final int REQUIRED_CROUCH_TICKS = 20;

    /** A jump before activation restarts the full one-second hold. */
    public static int advanceHold(int previousTicks, boolean grounded) {
        if (!grounded) return 0;
        return Math.min(REQUIRED_CROUCH_TICKS, Math.max(previousTicks, 0) + 1);
    }

    /** A pursuer already targeting the player cannot be fooled by a new box. */
    public static boolean mayActivate(int heldTicks, boolean currentlyHunted) {
        return heldTicks >= REQUIRED_CROUCH_TICKS && !currentlyHunted;
    }
}

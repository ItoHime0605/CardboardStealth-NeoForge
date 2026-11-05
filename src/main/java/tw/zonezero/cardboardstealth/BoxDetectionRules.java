package tw.zonezero.cardboardstealth;

/**
 * The box hides a player from NEW target selection outside the relevant recognition radius.
 * Pre-existing targets are handled by BoxState/BoxAggro, never erased by this class.
 * Pure Java so the boundary rules can be tested without loading Minecraft.
 */
public final class BoxDetectionRules {
    private BoxDetectionRules() {}

    // Stationary cardboard resembles a prop; shuffling cardboard is easier to notice.
    public static final double STATIONARY_RECOGNITION_RADIUS = 1.25D;
    public static final double MOVING_RECOGNITION_RADIUS = 3.50D;

    public static double recognitionRadius(boolean moving) {
        return moving ? MOVING_RECOGNITION_RADIUS : STATIONARY_RECOGNITION_RADIUS;
    }

    public static boolean concealsNewTarget(double distanceSquared, boolean moving) {
        double radius = recognitionRadius(moving);
        // The boundary itself is detectable, so only *beyond* the radius is concealed.
        return Double.isFinite(distanceSquared) && distanceSquared > radius * radius;
    }
}

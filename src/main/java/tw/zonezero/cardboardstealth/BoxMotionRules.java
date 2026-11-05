package tw.zonezero.cardboardstealth;

/**
 * The player continues to count as moving during five stationary ticks.
 * This is shared by the server's stealth radius and the client's settled
 * cardboard appearance. A slow TPS changes real-world duration, not tick count.
 */
public final class BoxMotionRules {
    private BoxMotionRules() {}
    public static final int SETTLE_TICKS = 5;
    public static final double EPSILON_SQR = 0.0001D;

    public static long movingUntil(long motionTick) {
        return motionTick + SETTLE_TICKS - 1L;
    }

    public static boolean countsAsMoving(long currentTick, long lastMotionUntil) {
        return currentTick <= lastMotionUntil;
    }

    public static boolean moved(double dx, double dy, double dz, boolean grounded) {
        return !grounded || dx * dx + dy * dy + dz * dz > EPSILON_SQR;
    }
}

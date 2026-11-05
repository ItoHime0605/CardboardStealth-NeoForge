package tw.zonezero.cardboardstealth;

/** Scale-free model animation rules; all offsets are render-only, never hitbox changes. */
public final class BoxModelPose {
    private BoxModelPose() {}
    public static float liftPixels(boolean rested) {
        return rested ? 0F : 3F;
    }
    /** Backward tip as the player steps up a slab (but not during a jump). */
    public static float stepBackTilt(double ascendingDeltaY, boolean grounded, boolean rested) {
        if (!grounded || rested || ascendingDeltaY < 0.025D || ascendingDeltaY > 0.55D) return 0F;
        return -(float) Math.min(0.105D, ascendingDeltaY * 0.25D);
    }
}

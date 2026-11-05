package tw.zonezero.cardboardstealth;

/** Purely visual poses; model pixels, no collision changes or slab tilt. */
public final class BoxModelPose {
    private BoxModelPose() {}
    /** Negative lift is the 8px downward resting position. */
    public static float liftPixels(boolean rested) { return rested ? -8F : 3F; }
    /** Walking flaps angle outwards; rested flaps lie nearly horizontal. */
    /** At rest the four lid flaps are perfectly horizontal, not 0.7px underground. */
    public static float flapAngle(boolean rested) {
        return rested ? (float) (Math.PI / 2.0D) : 0.62F;
    }
    /** Apply bodyYaw-headYaw after Minecraft already rotates the body renderer. */
    public static float lookRelativeYaw(float bodyYaw, float headYaw) {
        float relative = (bodyYaw - headYaw) % 360F;
        if (relative > 180F) relative -= 360F;
        if (relative < -180F) relative += 360F;
        return relative;
    }
}

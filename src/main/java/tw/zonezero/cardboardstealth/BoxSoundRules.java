package tw.zonezero.cardboardstealth;

/**
 * Single sound-name policy shared by the server, local client and Zombie Awareness.
 * Source attribution is handled independently by the sound-origin mixin.
 */
public final class BoxSoundRules {
    private BoxSoundRules() {}

    public static boolean isStep(String path) {
        return path != null && (path.endsWith(".step") || path.endsWith("_step"));
    }

    public static boolean isLanding(String path) {
        return path != null && (path.endsWith(".land") ||
                path.endsWith(".fall") || path.endsWith("_fall"));
    }
}

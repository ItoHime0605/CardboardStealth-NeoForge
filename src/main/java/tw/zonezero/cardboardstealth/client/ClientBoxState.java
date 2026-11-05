package tw.zonezero.cardboardstealth.client;

import java.util.HashMap;
import java.util.Map;

/** The server remains authoritative for the appearance and five-tick rest state. */
public final class ClientBoxState {
    private record BoxVisual(int variant, boolean rested) {}
    private static final Map<Integer, BoxVisual> VISUALS = new HashMap<>();

    public static void receive(int entityId, int variant, boolean rested) {
        if (variant == 0) VISUALS.remove(entityId);
        else VISUALS.put(entityId, new BoxVisual(variant, rested));
    }

    public static int variant(int entityId) {
        BoxVisual state = VISUALS.get(entityId);
        return state == null ? 0 : state.variant();
    }

    public static boolean rested(int entityId) {
        BoxVisual state = VISUALS.get(entityId);
        return state != null && state.rested();
    }

    public static void clear() { VISUALS.clear(); }
}

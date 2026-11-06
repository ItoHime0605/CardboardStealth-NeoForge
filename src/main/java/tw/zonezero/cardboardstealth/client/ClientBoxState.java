package tw.zonezero.cardboardstealth.client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import tw.zonezero.cardboardstealth.DecalStackRules;

/** Server-authoritative worn box color, settled state and decoration order. */
public final class ClientBoxState {
    private record BoxVisual(int variant, boolean rested, int[] decals) {}
    private static final Map<Integer, BoxVisual> VISUALS = new HashMap<>();

    public static void receive(int entityId, int variant, boolean rested,
                               int decal0, int decal1, int decal2) {
        if (variant == 0) {
            VISUALS.remove(entityId);
            return;
        }
        int[] raw = {decal0, decal1, decal2};
        VISUALS.put(entityId, new BoxVisual(variant, rested,
                DecalStackRules.sanitized(raw)));
    }
    public static int variant(int entityId) {
        BoxVisual v = VISUALS.get(entityId);
        return v == null ? 0 : v.variant();
    }
    public static boolean rested(int entityId) {
        BoxVisual v = VISUALS.get(entityId);
        return v != null && v.rested();
    }
    public static int[] decals(int entityId) {
        BoxVisual v = VISUALS.get(entityId);
        return v == null ? new int[0] : Arrays.copyOf(v.decals(), v.decals().length);
    }
    public static void clear() { VISUALS.clear(); }
}

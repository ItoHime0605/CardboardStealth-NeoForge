package tw.zonezero.cardboardstealth.client;

import java.util.HashMap;
import java.util.Map;

public final class ClientBoxState {
    private static final Map<Integer, Integer> VARIANTS = new HashMap<>();
    public static void receive(int entityId, int variant) {
        if (variant == 0) VARIANTS.remove(entityId);
        else VARIANTS.put(entityId, variant);
    }
    public static int variant(int entityId) { return VARIANTS.getOrDefault(entityId, 0); }
    public static void clear() { VARIANTS.clear(); }
}

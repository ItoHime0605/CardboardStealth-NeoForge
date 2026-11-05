package tw.zonezero.cardboardstealth;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

/**
 * Lightweight, renderer-independent list filter for one player's render pass.
 * The renderer's original attached layers and order are restored after each pass.
 * No Mixin injection into Minecraft's rendering method is required.
 */
public final class BoxLayerMasking {
    private BoxLayerMasking() {}

    public static <T> List<T> retainDuringRender(List<T> attached, Predicate<? super T> allowed) {
        List<T> snapshot = new ArrayList<>(attached);
        attached.removeIf(layer -> !allowed.test(layer));
        return snapshot;
    }

    public static <T> void restoreAfterRender(List<T> attached, List<? extends T> snapshot) {
        attached.clear();
        attached.addAll(snapshot);
    }
}

package tw.zonezero.cardboardstealth.client;

import java.lang.reflect.Field;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import tw.zonezero.cardboardstealth.BoxLayerMasking;

/**
 * Suppress third-person attached visuals without injecting into the renderer.
 *
 * The 0.1.17-alpha @Redirect in LivingEntityRenderer.render crashed on
 * NeoForge 21.1.264 ("0/1 succeeded, Scanned 0 target(s)"). Instead this
 * code runs through the stable RenderPlayerEvent.Pre / .Post event pair.
 *
 * Minecraft does not expose its attached-render-layer collection publicly in
 * 1.21.1. We find the list by runtime content/type (including our CardboardLayer)
 * rather than depending on a mapped/obfuscated field name or render signature.
 * If another renderer makes the list inaccessible, log it and leave the client
 * running instead of forcing a critical Mixin startup failure.
 */
public final class ClientLayerRenderControl {
    private static final Logger LOG = LoggerFactory.getLogger("cardboardstealth");
    private static final Map<PlayerRenderer, List<RenderLayer<?, ?>>> SAVED = new IdentityHashMap<>();
    private static boolean warned;

    private ClientLayerRenderControl() {}

    @SuppressWarnings("unchecked")
    private static List<RenderLayer<?, ?>> attachedLayers(PlayerRenderer renderer) {
        for (Field field : LivingEntityRenderer.class.getDeclaredFields()) {
            if (!List.class.isAssignableFrom(field.getType())) continue;
            try {
                field.setAccessible(true);
                Object fieldValue = field.get(renderer);
                if (!(fieldValue instanceof List<?> layers)) continue;
                // Multiple mods can add layers. Only the actual attached-layer list
                // contains the custom cardboard layer registered in AddLayers.
                if (layers.stream().anyMatch(layer -> layer instanceof CardboardLayer)) {
                    return (List<RenderLayer<?, ?>>) (List<?>) layers;
                }
            } catch (ReflectiveOperationException | RuntimeException ex) {
                warn("Cannot inspect Minecraft's attached-render-layer list", ex);
            }
        }
        warn("Could not locate the player renderer's layer list; cardboard will not hide extra armor layers", null);
        return null;
    }

    public static void before(PlayerRenderer renderer) {
        // Defensive restoration of a stale snapshot if a previous rendering
        // pass was interrupted before its Post event.
        after(renderer);
        List<RenderLayer<?, ?>> layers = attachedLayers(renderer);
        if (layers == null) return;
        try {
            List<RenderLayer<?, ?>> snapshot = BoxLayerMasking.retainDuringRender(
                    layers, layer -> layer instanceof CardboardLayer);
            SAVED.put(renderer, snapshot);
        } catch (RuntimeException ex) {
            warn("Could not conceal the player renderer's equipment layers", ex);
        }
    }

    public static void after(PlayerRenderer renderer) {
        List<RenderLayer<?, ?>> snapshot = SAVED.remove(renderer);
        if (snapshot == null) return;
        List<RenderLayer<?, ?>> layers = attachedLayers(renderer);
        if (layers == null) return;
        try {
            BoxLayerMasking.restoreAfterRender(layers, snapshot);
        } catch (RuntimeException ex) {
            warn("Could not restore the player renderer's original layers", ex);
        }
    }

    public static void reset() {
        for (PlayerRenderer renderer : List.copyOf(SAVED.keySet())) after(renderer);
        SAVED.clear();
    }

    private static void warn(String message, Exception exception) {
        if (warned) return;
        warned = true;
        if (exception == null) LOG.warn("[Cardboard Stealth] {}", message);
        else LOG.warn("[Cardboard Stealth] {}", message, exception);
    }
}

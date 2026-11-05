package tw.zonezero.cardboardstealth;

import java.util.ArrayDeque;
import java.util.Optional;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

/** Records the *actual* source of an entity-bound sound, not merely its XYZ location. */
public final class BoxSoundContext {
    private BoxSoundContext() {}
    private static final ThreadLocal<ArrayDeque<Optional<Entity>>> SOURCES =
            ThreadLocal.withInitial(ArrayDeque::new);

    public static void enter(Entity origin) {
        SOURCES.get().push(Optional.ofNullable(origin));
    }

    public static void exit() {
        var stack = SOURCES.get();
        if (!stack.isEmpty()) stack.pop();
        if (stack.isEmpty()) SOURCES.remove();
    }

    public static Player disguisedOrigin() {
        var stack = SOURCES.get();
        if (stack.isEmpty()) return null;
        Entity origin = stack.peek().orElse(null);
        return origin instanceof Player player && BoxState.active(player) ? player : null;
    }
}

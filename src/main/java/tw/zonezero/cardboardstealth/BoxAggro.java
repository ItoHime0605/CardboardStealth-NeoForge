package tw.zonezero.cardboardstealth;

import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.WeakHashMap;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;

/**
 * Only remembers mobs that actually selected a player as a target.
 * It never invents a distance-based "combat timer", and it never clears a mob's target.
 * Weak keys prevent tracking despawned mobs forever.
 */
public final class BoxAggro {
    private BoxAggro() {}
    private static final Map<Mob, UUID> TARGETED_PLAYERS = new WeakHashMap<>();

    public static void update(Mob mob, Player target) {
        if (target == null) TARGETED_PLAYERS.remove(mob);
        else TARGETED_PLAYERS.put(mob, target.getUUID());
    }

    public static boolean isBeingHunted(ServerPlayer player) {
        // The event-backed map handles mobs that have chased a player far away.
        Iterator<Map.Entry<Mob, UUID>> it = TARGETED_PLAYERS.entrySet().iterator();
        boolean pursued = false;
        while (it.hasNext()) {
            Map.Entry<Mob, UUID> entry = it.next();
            Mob mob = entry.getKey();
            if (mob == null || mob.isRemoved() || !mob.isAlive() ||
                    !(mob.getTarget() instanceof Player current) ||
                    !current.getUUID().equals(entry.getValue())) {
                it.remove();
                continue;
            }
            if (entry.getValue().equals(player.getUUID()) && mob.level() == player.level()) {
                pursued = true;
            }
        }
        if (pursued) return true;

        // Fallback for targets acquired before the mod was installed, or by AI that skipped
        // LivingChangeTargetEvent. Runs ONLY on a disguise activation attempt, never every tick.
        return !player.serverLevel().getEntitiesOfClass(Mob.class,
                player.getBoundingBox().inflate(48.0), BoxState::fooled).stream()
                .filter(mob -> mob.getTarget() == player).toList().isEmpty();
    }
}

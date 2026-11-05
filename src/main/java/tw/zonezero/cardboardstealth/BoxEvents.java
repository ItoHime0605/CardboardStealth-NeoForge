package tw.zonezero.cardboardstealth;

import com.corosus.zombieawareness.ZAUtil;
import com.corosus.zombieawareness.EnumSenseType;
import com.corosus.zombieawareness.config.ZAConfigGeneral;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.PlayLevelSoundEvent;
import net.neoforged.neoforge.event.entity.living.LivingChangeTargetEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingEvent;
import net.neoforged.neoforge.event.entity.player.AttackEntityEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.joml.Vector3d;

public final class BoxEvents {
    private BoxEvents() {}
    public static void playerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        int equipped = BoxState.equippedVariant(player);
        // After activation the box stays on through its shortened jumps.
        boolean eligible = equipped != 0 && player.isShiftKeyDown() &&
                !player.isPassenger() && !player.isSwimming() && !player.isFallFlying() &&
                !player.getAbilities().flying && player.isAlive() &&
                player.level().getGameTime() >= BoxState.blockedUntil(player);
        if (!eligible) {
            BoxState.hold(player, 0);
            BoxState.set(player, 0);
            return;
        }
        if (BoxState.active(player)) {
            if (equipped != BoxState.variant(player)) BoxState.set(player, equipped);
            BoxState.updateMotion(player);
            return;
        }
        // Activation itself requires stable ground; airborne movement is allowed afterwards.
        if (!player.onGround()) {
            BoxState.hold(player, 0);
            return;
        }
        int held = Math.min(BoxState.hold(player) + 1, BoxState.ACTIVATE_TICKS);
        if (held < BoxState.ACTIVATE_TICKS) {
            BoxState.hold(player, held);
            return;
        }
        // Do not allow instant safe disguise while any mob is still chasing this player.
        if (BoxAggro.isBeingHunted(player)) {
            BoxState.hold(player, 0);
            return;
        }
        BoxState.hold(player, held);
        BoxState.set(player, equipped);
    }

    public static void changeTarget(LivingChangeTargetEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !BoxState.fooled(mob)) return;
        if (event.getNewAboutToBeSetTarget() instanceof ServerPlayer player &&
                BoxState.concealedFrom(player, mob)) {
            // No prior lock: stop only this *new* target selection. Old paths/scent remain.
            event.setNewAboutToBeSetTarget(null);
        }
        BoxAggro.update(mob, event.getNewAboutToBeSetTarget() instanceof Player player ? player : null);
    }

    public static void jump(LivingEvent.LivingJumpEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !BoxState.active(player)) return;
        BoxMovement.limitJump(player);
        // A small but nonzero ZA sound trace: about 12.5% of its configured sound strength.
        int strength = Math.max(1, ZAConfigGeneral.soundStrength / 8);
        ZAUtil.spawnOrBuffSenseAtPos(player.level(),
                new Vector3d(player.getX(), player.getY(), player.getZ()),
                EnumSenseType.SOUND, strength);
    }

    public static void stepSound(PlayLevelSoundEvent.AtEntity event) {
        if (!(event.getEntity() instanceof Player player) || !BoxState.active(player) || event.getSound() == null) return;
        String path = event.getSound().value().getLocation().getPath();
        if (path.endsWith(".step")) event.setNewVolume(event.getNewVolume() * 0.18F);
        else if (path.endsWith(".land") || path.endsWith(".fall"))
            event.setNewVolume(event.getNewVolume() * 0.28F);
    }

    public static void damaged(LivingDamageEvent.Post event) {
        // Taking a hit or successfully hurting another entity breaks the disguise.
        // This covers projectile/hitscan damage where AttackEntityEvent never fires.
        if (event.getEntity() instanceof ServerPlayer victim && BoxState.active(victim))
            BoxState.interrupt(victim, 20);
        if (event.getSource().getEntity() instanceof ServerPlayer attacker && BoxState.active(attacker))
            BoxState.interrupt(attacker, 20);
    }
    public static void useItem(PlayerInteractEvent.RightClickItem event) {
        // Do not permit item activation while remaining visually disguised.
        if (event.getEntity() instanceof ServerPlayer player && BoxState.active(player)
                && !event.getItemStack().isEmpty()) BoxState.interrupt(player, 20);
    }
    public static void attack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && BoxState.active(player))
            BoxState.interrupt(player, 20);
    }
    public static void leftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player && BoxState.active(player))
            BoxState.interrupt(player, 20);
    }
    public static void playerJoin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) BoxState.interrupt(player, 0);
    }
    public static void respawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) BoxState.interrupt(player, 0);
    }
    public static void changedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) BoxState.interrupt(player, 0);
    }
    public static void playerLeave(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) BoxState.interrupt(player, 0);
    }
    public static void startTracking(PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof ServerPlayer viewer && event.getTarget() instanceof ServerPlayer target)
            ModNetwork.send(viewer, target, BoxState.variant(target));
    }
    public static void stopTracking(PlayerEvent.StopTracking event) {
        if (event.getEntity() instanceof ServerPlayer viewer && event.getTarget() instanceof ServerPlayer target)
            ModNetwork.send(viewer, target, 0);
    }
}

package tw.zonezero.cardboardstealth;

import com.corosus.zombieawareness.ZAUtil;
import com.corosus.zombieawareness.EnumSenseType;
import com.corosus.zombieawareness.config.ZAConfigGeneral;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
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
        // Eating, drinking, equipping items and using a pistol are permitted.
        // Heavy TaCZ firearms immediately break concealment even without firing.
        if (BoxState.active(player) && ModList.get().isLoaded("tacz")
                && TaczCompat.isNonPistolGun(player.getMainHandItem())) {
            BoxState.interrupt(player, 20);
            return;
        }
        boolean eligible = equipped != 0 && player.isShiftKeyDown() &&
                !player.isPassenger() && !player.isSwimming() && !player.isFallFlying() &&
                !player.getAbilities().flying && player.isAlive() &&
                !(ModList.get().isLoaded("tacz") && TaczCompat.isNonPistolGun(player.getMainHandItem())) &&
                player.level().getGameTime() >= BoxState.blockedUntil(player);
        if (!eligible) {
            if (BoxState.hold(player) != 0) BoxState.hold(player, 0);
            if (BoxState.active(player)) BoxState.set(player, 0);
            return;
        }
        if (BoxState.active(player)) {
            if (equipped != BoxState.variant(player)) BoxState.set(player, equipped);
            BoxState.updateMotion(player);
            return;
        }
        // Stationary activation requires twenty consecutive grounded crouch ticks.
        // Once activated, short jumps are permitted and don't reset the disguise.
        int held = BoxActivationRules.advanceHold(BoxState.hold(player), player.onGround());
        if (held < BoxState.ACTIVATE_TICKS) {
            BoxState.hold(player, held);
            return;
        }
        // Query aggression only after the full hold, not on every crouching tick.
        if (!BoxActivationRules.mayActivate(held, BoxAggro.isBeingHunted(player))) {
            BoxState.hold(player, 0);
            return;
        }
        BoxState.hold(player, held);
        BoxState.set(player, equipped);
    }

    public static void changeTarget(LivingChangeTargetEvent event) {
        // Respect target changes already cancelled by another mod.
        if (event.isCanceled() || !(event.getEntity() instanceof Mob mob) || !BoxState.fooled(mob)) return;
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
        // Follow Zombie Awareness's own dimension/sound enablement rules.
        // Disabled ZA hearing must never be re-enabled by a disguised jump.
        if (ZAUtil.canSpawnTraceQuickCheck(player.level())) {
            int strength = Math.max(1, ZAConfigGeneral.soundStrength / 8);
            ZAUtil.spawnOrBuffSenseAtPos(player.level(),
                    new Vector3d(player.getX(), player.getY(), player.getZ()),
                    EnumSenseType.SOUND, strength);
        }
    }

    public static void stepSound(PlayLevelSoundEvent.AtEntity event) {
        if (!(event.getEntity() instanceof Player player) || !BoxState.active(player) || event.getSound() == null) return;
        String path = event.getSound().value().getLocation().getPath();
        if (BoxSoundRules.isStep(path)) event.setNewVolume(event.getNewVolume() * 0.18F);
        else if (BoxSoundRules.isLanding(path))
            event.setNewVolume(event.getNewVolume() * 0.28F);
    }

    public static void damaged(LivingDamageEvent.Post event) {
        // Being hit still breaks disguise. Outgoing pistol projectile damage does NOT:
        // melee is instead handled by AttackEntityEvent / LeftClickEmpty.
        if (event.getEntity() instanceof ServerPlayer victim && BoxState.active(victim))
            BoxState.interrupt(victim, 20);
    }
    public static void attack(AttackEntityEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && BoxState.active(player))
            BoxState.interrupt(player, 20);
    }
    public static void leftClickBlock(PlayerInteractEvent.LeftClickBlock event) {
        if (event.getEntity() instanceof ServerPlayer player && BoxState.active(player)
                && !(ModList.get().isLoaded("tacz") && TaczCompat.isPistol(player.getMainHandItem())))
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

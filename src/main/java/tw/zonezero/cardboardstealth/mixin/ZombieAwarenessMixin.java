package tw.zonezero.cardboardstealth.mixin;

import com.corosus.zombieawareness.ZAUtil;
import com.corosus.zombieawareness.EnumSenseType;
import com.corosus.zombieawareness.config.ZAConfigGeneral;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tw.zonezero.cardboardstealth.BoxState;
import tw.zonezero.cardboardstealth.BoxSoundContext;
import tw.zonezero.cardboardstealth.BoxSoundRules;
import org.joml.Vector3d;

@Mixin(value = ZAUtil.class, remap = false)
public abstract class ZombieAwarenessMixin {
    // After ZA's wandering-horde waypoint handling, but before creating blood scent.
    @Inject(method = "tickPlayer", at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC,
            target = "Lcom/corosus/zombieawareness/config/ZAConfigFeatures;awareness_Scent:Z"),
            cancellable = true)
    private static void cardboardstealth$muteScent(Player player, CallbackInfo ci) {
        if (BoxState.active(player)) ci.cancel();
    }

    // This is the custom ZA target scan, not only vanilla Mob.setTarget().
    @Inject(method = "isEnemy(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/entity/Entity;Z)Z",
            at = @At("HEAD"), cancellable = true)
    private static void cardboardstealth$ignorePlayer(Entity chaser, Entity candidate, boolean omniscient,
            CallbackInfoReturnable<Boolean> cir) {
        if (chaser instanceof Mob mob && BoxState.fooled(mob)
                && candidate instanceof Player player && BoxState.concealedFrom(player, mob))
            cir.setReturnValue(false);
    }

    // Only suppress the actual disguised player's own footsteps, never nearby bystanders.
    @Inject(method = "hookSoundEvent", at = @At("HEAD"), cancellable = true)
    private static void cardboardstealth$quietStep(SoundEvent sound, Level level, double x, double y, double z,
            float volume, float pitch, CallbackInfo ci) {
        if (sound == null || level.isClientSide()) return;
        Player source = BoxSoundContext.disguisedOrigin();
        if (source == null || source.level() != level) return;
        String path = sound.getLocation().getPath();
        boolean step = BoxSoundRules.isStep(path);
        boolean land = BoxSoundRules.isLanding(path);
        if (!step && !land) return;

        // Paper cardboard is not perfectly silent: weak sound sense traces remain.
        // Avoid spawning duplicate traces from the same player on a single tick.
        long now = level.getGameTime();
        String key = "cardboardstealth_last_footstep_tick";
        if (ZAUtil.canSpawnTraceQuickCheck(level) && source.getPersistentData().getLong(key) != now) {
            int strength = Math.max(1, ZAConfigGeneral.soundStrength / (land ? 8 : 16));
            ZAUtil.spawnOrBuffSenseAtPos(level, new Vector3d(x, y, z),
                    EnumSenseType.SOUND, strength);
            source.getPersistentData().putLong(key, now);
        }
        ci.cancel();
    }
}

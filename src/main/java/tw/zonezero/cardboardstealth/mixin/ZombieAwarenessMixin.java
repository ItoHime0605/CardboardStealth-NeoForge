package tw.zonezero.cardboardstealth.mixin;

import com.corosus.zombieawareness.ZAUtil;
import com.corosus.zombieawareness.config.ZAConfigFeatures;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tw.zonezero.cardboardstealth.BoxState;

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

    // Stop only disguised players' local FOOTSTEP sound from generating ZA sound traces.
    // Weapon fire, explosions, block damage, etc. remain detectable by ZA.
    @Inject(method = "hookSoundEvent", at = @At("HEAD"), cancellable = true)
    private static void cardboardstealth$quietStep(SoundEvent sound, Level level, double x, double y, double z,
            float volume, float pitch, CallbackInfo ci) {
        if (!(level instanceof ServerLevel server) || sound == null) return;
        String path = sound.getLocation().getPath();
        if (!path.endsWith(".step")) return;
        for (ServerPlayer player : server.players()) {
            if (BoxState.active(player) && player.distanceToSqr(x, y, z) <= 1.0D) {
                ci.cancel();
                return;
            }
        }
    }
}

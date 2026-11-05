package tw.zonezero.cardboardstealth.mixin;

import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tw.zonezero.cardboardstealth.BoxSoundContext;

/**
 * Wrap ServerLevel's entity-bound sound dispatch with source identity.
 * This enters before Zombie Awareness's own mixin at priority 1000.
 */
@Mixin(value = ServerLevel.class, priority = 1100)
public abstract class SoundOriginMixin {
    @Inject(method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V",
            at = @At("HEAD"))
    private void cardboardstealth$enter(Player excluded, Entity origin, Holder<SoundEvent> sound,
            SoundSource category, float volume, float pitch, long seed, CallbackInfo ci) {
        BoxSoundContext.enter(origin);
    }

    @Inject(method = "playSeededSound(Lnet/minecraft/world/entity/player/Player;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/core/Holder;Lnet/minecraft/sounds/SoundSource;FFJ)V",
            at = @At("RETURN"))
    private void cardboardstealth$exit(Player excluded, Entity origin, Holder<SoundEvent> sound,
            SoundSource category, float volume, float pitch, long seed, CallbackInfo ci) {
        BoxSoundContext.exit();
    }
}

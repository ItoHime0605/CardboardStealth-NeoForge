package tw.zonezero.cardboardstealth.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import tw.zonezero.cardboardstealth.client.ClientBoxState;

/**
 * Hide all held items in third-person player rendering while inside a box.
 * TaCZ 1.1.8 appends additional offhand/hotbar gun models to THIS layer,
 * so cancelling its render at HEAD also suppresses those guns.
 *
 * First-person gun/hand rendering is performed through RenderHandEvent,
 * not this entity layer, and must remain untouched for the local player.
 */
@Mixin(value = ItemInHandLayer.class, priority = 1200)
public abstract class ClientItemInHandMixin {
    @Inject(
            method = "render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V",
            at = @At("HEAD"), cancellable = true)
    private void cardboardstealth$hideBoxedHands(PoseStack pose, MultiBufferSource buffer, int packedLight,
            LivingEntity entity, float limbSwing, float limbSwingAmount, float partialTicks,
            float ageInTicks, float netHeadYaw, float headPitch, CallbackInfo ci) {
        if (entity instanceof Player player && ClientBoxState.variant(player.getId()) != 0) {
            ci.cancel();
        }
    }
}

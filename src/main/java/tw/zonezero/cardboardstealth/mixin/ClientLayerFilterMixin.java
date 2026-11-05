package tw.zonezero.cardboardstealth.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import tw.zonezero.cardboardstealth.BoxRenderRules;
import tw.zonezero.cardboardstealth.client.CardboardLayer;
import tw.zonezero.cardboardstealth.client.ClientBoxState;

/**
 * Player-only visual filter. All attached third-person layers are hidden except
 * our temporary box layer, so armor, cape, wings, held TaCZ gun, hotbar gun,
 * and other renderer layers cannot visibly protrude outside the box.
 *
 * Vanilla legs are part of PlayerModel, not separate layers, and remain visible.
 * This mixin never intercepts first-person hand/gun rendering.
 *
 * The erased method parameter in RenderLayer<T extends LivingEntity> is
 * LivingEntity, not Entity. Keep require=1 so a broken render hook cannot
 * silently leave players' guns exposed in the loaded game.
 */
@Mixin(value = LivingEntityRenderer.class, priority = 1100)
public abstract class ClientLayerFilterMixin {
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Redirect(
            method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(value = "INVOKE", target =
                    "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/LivingEntity;FFFFFF)V"),
            require = 1)
    private void cardboardstealth$renderVisibleLayers(RenderLayer layer, PoseStack pose,
            MultiBufferSource buffers, int light, LivingEntity entity, float limbSwing,
            float limbSwingAmount, float partialTick, float ageInTicks,
            float netHeadYaw, float headPitch) {
        boolean disguised = entity instanceof Player &&
                ClientBoxState.variant(entity.getId()) != 0;
        if (BoxRenderRules.renderAttachedLayer(disguised, layer instanceof CardboardLayer)) {
            layer.render(pose, buffers, light, entity, limbSwing, limbSwingAmount,
                    partialTick, ageInTicks, netHeadYaw, headPitch);
        }
    }
}

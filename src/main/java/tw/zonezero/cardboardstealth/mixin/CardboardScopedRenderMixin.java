package tw.zonezero.cardboardstealth.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import tw.zonezero.cardboardstealth.BoxRenderRules;
import tw.zonezero.cardboardstealth.client.CardboardLayer;
import tw.zonezero.cardboardstealth.client.ClientBoxState;

/**
 * Per-render-call concealment rather than mutation of shared render objects.
 *
 * No PlayerModel.visible flags, no reflection on LivingEntityRenderer.layers,
 * no Pre/Post pairs, no global texture/shader states and NO saved snapshots.
 * A disguised player is drawn as the dedicated CardboardLayer; all other
 * layers are skipped for THIS invocation only. The next render of the same
 * or another player is completely unaffected even if a render is cancelled.
 *
 * WrapOperation chains with other MixinExtras wrappers rather than replacing
 * their operations with a conflict-prone @Redirect.
 */
@Mixin(value = LivingEntityRenderer.class, priority = 900)
public abstract class CardboardScopedRenderMixin {
    private static boolean cardboardstealth$disguised(Entity entity) {
        return entity instanceof AbstractClientPlayer player
                && ClientBoxState.variant(player.getId()) != 0;
    }

    /** A null body RenderType skips the vanilla SKIN and its colored vertices. */
    @Inject(method = "getRenderType", at = @At("RETURN"), cancellable = true)
    private void cardboardstealth$hideOnlyThisPlayerBody(LivingEntity entity,
            boolean bodyVisible, boolean translucent, boolean glowing,
            CallbackInfoReturnable<RenderType> cir) {
        if (!BoxRenderRules.renderBody(cardboardstealth$disguised(entity))) {
            cir.setReturnValue(null);
        }
    }

    /**
     * The original layer list remains untouched and in exactly its old order.
     * Only skip draw calls for a specific boxed player, except the box itself.
     * When the disguise disappears, these calls immediately run normally.
     * No restoration phase can be skipped, since no state was modified.
     */
    @WrapOperation(method = "render",
            at = @At(value = "INVOKE",
                target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;render(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;ILnet/minecraft/world/entity/Entity;FFFFFF)V"))
    @SuppressWarnings({"rawtypes", "unchecked"})
    private void cardboardstealth$renderOnlyBoxLayer(RenderLayer layer,
            PoseStack matrices, MultiBufferSource buffers, int packedLight,
            Entity renderedEntity, float limbSwing, float limbSwingAmount,
            float partialTick, float ageInTicks, float netHeadYaw, float headPitch,
            Operation<Void> original) {
        if (!BoxRenderRules.renderAttachedLayer(
                cardboardstealth$disguised(renderedEntity), layer instanceof CardboardLayer)) {
            return;
        }
        original.call(layer, matrices, buffers, packedLight, renderedEntity,
                limbSwing, limbSwingAmount, partialTick, ageInTicks,
                netHeadYaw, headPitch);
    }
}

package tw.zonezero.cardboardstealth.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import tw.zonezero.cardboardstealth.BoxColors;
import tw.zonezero.cardboardstealth.BoxModelPose;
import tw.zonezero.cardboardstealth.CardboardStealth;

/**
 * Actual hollow rectangular box, built from individual cardboard panels.
 *
 * Orientation: forward = -Z, rear (+Z) bears the printed recycling logo.
 * The eye opening is a real hole in the front wall (not a painted rectangle).
 * The four lower closure flaps fold outward, leaving the bottom centre open.
 */
public final class CardboardLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation PAPER = ResourceLocation.fromNamespaceAndPath(
            CardboardStealth.MOD_ID, "textures/entity/paper.png");
    private static final ResourceLocation TAPE = ResourceLocation.fromNamespaceAndPath(
            CardboardStealth.MOD_ID, "textures/entity/tape.png");
    private static final ResourceLocation RECYCLE = ResourceLocation.fromNamespaceAndPath(
            CardboardStealth.MOD_ID, "textures/entity/recycle_logo.png");
    private final ModelPart paper;
    private final ModelPart tape;
    private final ModelPart recycle;

    public CardboardLayer(PlayerRenderer parent, ModelPart root) {
        super(parent);
        paper = root.getChild("paper");
        tape = root.getChild("tape");
        recycle = root.getChild("recycle");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition paper = root.addOrReplaceChild("paper", CubeListBuilder.create(), PartPose.ZERO);

        // Body: 18px (1.125 blocks) wide, 22px (1.375 blocks) long,
        // 21px high. Each wall is 0.75px thick instead of a solid cube.
        paper.addOrReplaceChild("roof", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-9F, -5F, -11F, 18F, 0.75F, 22F), PartPose.ZERO);
        paper.addOrReplaceChild("back", CubeListBuilder.create().texOffs(0, 24)
                .addBox(-9F, -4.25F, 10.25F, 18F, 20.25F, 0.75F), PartPose.ZERO);
        paper.addOrReplaceChild("left", CubeListBuilder.create().texOffs(42, 24)
                .addBox(-9F, -4.25F, -11F, 0.75F, 20.25F, 22F), PartPose.ZERO);
        paper.addOrReplaceChild("right", CubeListBuilder.create().texOffs(42, 24)
                .addBox(8.25F, -4.25F, -11F, 0.75F, 20.25F, 22F), PartPose.ZERO);

        // Split the -Z front into four strips: open 10 x 4 pixels at eye level.
        paper.addOrReplaceChild("front_top", CubeListBuilder.create().texOffs(0, 49)
                .addBox(-9F, -4.25F, -11F, 18F, 4.25F, 0.75F), PartPose.ZERO);
        paper.addOrReplaceChild("front_bottom", CubeListBuilder.create().texOffs(0, 58)
                .addBox(-9F, 4F, -11F, 18F, 12F, 0.75F), PartPose.ZERO);
        paper.addOrReplaceChild("front_left", CubeListBuilder.create().texOffs(0, 75)
                .addBox(-9F, 0F, -11F, 4F, 4F, 0.75F), PartPose.ZERO);
        paper.addOrReplaceChild("front_right", CubeListBuilder.create().texOffs(0, 75)
                .addBox(5F, 0F, -11F, 4F, 4F, 0.75F), PartPose.ZERO);
        // Block the 1x1 corners to give the physical opening a pixel-rounded shape,
        // matching the first-person window reference rather than a square slot.
        paper.addOrReplaceChild("eye_corner_top_left", CubeListBuilder.create().texOffs(0, 75)
                .addBox(-5F, 0F, -11F, 1F, 1F, 0.75F), PartPose.ZERO);
        paper.addOrReplaceChild("eye_corner_top_right", CubeListBuilder.create().texOffs(0, 75)
                .addBox(4F, 0F, -11F, 1F, 1F, 0.75F), PartPose.ZERO);
        paper.addOrReplaceChild("eye_corner_bottom_left", CubeListBuilder.create().texOffs(0, 75)
                .addBox(-5F, 3F, -11F, 1F, 1F, 0.75F), PartPose.ZERO);
        paper.addOrReplaceChild("eye_corner_bottom_right", CubeListBuilder.create().texOffs(0, 75)
                .addBox(4F, 3F, -11F, 1F, 1F, 0.75F), PartPose.ZERO);

        // Bottom has no base plate: four separate 0.7px-thick 10px flaps
        // swing out ~35 degrees. At rest their tips reach ground level (Y24).
        paper.addOrReplaceChild("front_flap", CubeListBuilder.create().texOffs(0, 84)
                .addBox(-9F, 0F, -0.35F, 18F, 10F, 0.7F),
                PartPose.offsetAndRotation(0F, 16F, -11F, -0.62F, 0F, 0F));
        paper.addOrReplaceChild("back_flap", CubeListBuilder.create().texOffs(0, 84)
                .addBox(-9F, 0F, -0.35F, 18F, 10F, 0.7F),
                PartPose.offsetAndRotation(0F, 16F, 11F, 0.62F, 0F, 0F));
        paper.addOrReplaceChild("left_flap", CubeListBuilder.create().texOffs(42, 76)
                .addBox(-0.35F, 0F, -11F, 0.7F, 10F, 22F),
                PartPose.offsetAndRotation(-9F, 16F, 0F, 0F, 0F, 0.62F));
        paper.addOrReplaceChild("right_flap", CubeListBuilder.create().texOffs(42, 76)
                .addBox(-0.35F, 0F, -11F, 0.7F, 10F, 22F),
                PartPose.offsetAndRotation(9F, 16F, 0F, 0F, 0F, -0.62F));

        // Keep the packing tape neutral across all 16 dye variations.
        PartDefinition tape = root.addOrReplaceChild("tape", CubeListBuilder.create(), PartPose.ZERO);
        tape.addOrReplaceChild("top_seam", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-1.5F, -5.16F, -11F, 3F, 0.16F, 22F), PartPose.ZERO);
        tape.addOrReplaceChild("rear_fold", CubeListBuilder.create().texOffs(0, 27)
                .addBox(-1.5F, -5F, 11F, 3F, 4.7F, 0.10F), PartPose.ZERO);
        tape.addOrReplaceChild("front_fold", CubeListBuilder.create().texOffs(0, 27)
                .addBox(-1.5F, -5F, -11.10F, 3F, 4.7F, 0.10F), PartPose.ZERO);

        // Only the rear (+Z), lower-right area carries the green recycling stamp.
        PartDefinition recycle = root.addOrReplaceChild("recycle", CubeListBuilder.create(), PartPose.ZERO);
        recycle.addOrReplaceChild("rear_stamp", CubeListBuilder.create().texOffs(0, 0)
                .addBox(3.75F, 9.5F, 11.02F, 4.5F, 4.5F, 0.08F), PartPose.ZERO);

        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        int variant = ClientBoxState.variant(player.getId());
        if (variant == 0) return;
        boolean rested = ClientBoxState.rested(player.getId());
        float lift = BoxModelPose.liftPixels(rested);
        float lean = BoxModelPose.stepBackTilt(player.getY() - player.yOld, player.onGround(), rested);
        pose.pushPose();
        pose.translate(0D, -lift / 16D, 0D);
        // Tilt around the underside rather than spinning the box through the ground.
        if (lean != 0F) {
            pose.translate(0D, -0.5D, 0D);
            pose.mulPose(Axis.XP.rotation(lean));
            pose.translate(0D, 0.5D, 0D);
        }
        paper.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(PAPER)),
                light, OverlayTexture.NO_OVERLAY, 0xFF000000 | BoxColors.rgb(variant));
        tape.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TAPE)),
                light, OverlayTexture.NO_OVERLAY);
        recycle.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(RECYCLE)),
                light, OverlayTexture.NO_OVERLAY);
        pose.popPose();
    }
}

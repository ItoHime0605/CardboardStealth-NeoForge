package tw.zonezero.cardboardstealth.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
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
import net.minecraft.util.Mth;
import tw.zonezero.cardboardstealth.BoxColors;
import tw.zonezero.cardboardstealth.BoxModelGeometry;
import tw.zonezero.cardboardstealth.BoxModelPose;
import tw.zonezero.cardboardstealth.CardboardStealth;

/**
 * Layer-built 3D cardboard with a real narrow exterior slit. Unlike the interior
 * HUD mask, this slot is intentionally only 10.5 × 1.5 model pixels.
 *
 * - Forward = -Z: discreet peep opening, no printed recycling logo.
 * - Rear = +Z: recycling stamp in the lower right corner.
 * - Walking flaps angled, stationary flaps lie flat; a recessed liner hides
 *   the player's torso at the lower mouth while their feet remain visible.
 * - 16 dye body shades, neutral tape, pixel folds and genuine rear logo.
 * - All geometry is vanilla ModelPart cuboids, with restrained pixel paper grain.
 */
public final class CardboardLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation PAPER = ResourceLocation.fromNamespaceAndPath(
            CardboardStealth.MOD_ID, "textures/entity/paper_refined.png");
    private static final ResourceLocation TAPE = ResourceLocation.fromNamespaceAndPath(
            CardboardStealth.MOD_ID, "textures/entity/tape_refined.png");
    private static final ResourceLocation RECYCLE = ResourceLocation.fromNamespaceAndPath(
            CardboardStealth.MOD_ID, "textures/entity/recycle_stamp.png");

    private final ModelPart body;
    private final ModelPart sideShade;
    private final ModelPart foldedEdges;
    private final ModelPart undersideShade;
    private final ModelPart shadowInk;
    private final ModelPart tape;
    private final ModelPart eyeCavity;

    public CardboardLayer(PlayerRenderer parent, ModelPart root) {
        super(parent);
        body = root.getChild("body");
        sideShade = root.getChild("side_shade");
        foldedEdges = root.getChild("folded_edges");
        undersideShade = root.getChild("underside_shade");
        shadowInk = root.getChild("shadow_ink");
        tape = root.getChild("tape");
        eyeCavity = root.getChild("eye_cavity");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();

        // Roof, four walls and four bottom flaps have actual volume. No full
        // cardboard cube is placed over the player: the cavity is open.
        PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.ZERO);
        body.addOrReplaceChild("roof", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-9F, -5F, -11F, 18F, 0.72F, 22F), PartPose.ZERO);
        body.addOrReplaceChild("rear_wall", CubeListBuilder.create().texOffs(2, 24)
                .addBox(-9F, -4.28F, 10.28F, 18F, 20.28F, 0.72F), PartPose.ZERO);
        // Fine slot, 10.5 wide by 1.5 tall, located near the player's eye line.
        final float slitTop = BoxModelGeometry.SLIT_TOP_Y;
        final float slitBottom = BoxModelGeometry.SLIT_BOTTOM_Y;
        final float slitHalf = BoxModelGeometry.SLIT_HALF_WIDTH;
        body.addOrReplaceChild("front_above_eye", CubeListBuilder.create().texOffs(0, 49)
                .addBox(-9F, -4.28F, -11F, 18F, slitTop + 4.28F, 0.72F), PartPose.ZERO);
        body.addOrReplaceChild("front_below_eye", CubeListBuilder.create().texOffs(0, 59)
                .addBox(-9F, slitBottom, -11F, 18F, 16F - slitBottom, 0.72F), PartPose.ZERO);
        body.addOrReplaceChild("front_eye_left", CubeListBuilder.create().texOffs(2, 82)
                .addBox(-9F, slitTop, -11F, 9F - slitHalf, slitBottom - slitTop, 0.72F), PartPose.ZERO);
        body.addOrReplaceChild("front_eye_right", CubeListBuilder.create().texOffs(19, 82)
                .addBox(slitHalf, slitTop, -11F, 9F - slitHalf, slitBottom - slitTop, 0.72F), PartPose.ZERO);
        // Pixel bevels at the ends only; keep the middle unmistakably slit-thin.
        body.addOrReplaceChild("eye_bevel_left_top", CubeListBuilder.create().texOffs(0, 104)
                .addBox(-slitHalf, slitTop, -11F, 0.40F, 0.35F, 0.72F), PartPose.ZERO);
        body.addOrReplaceChild("eye_bevel_right_top", CubeListBuilder.create().texOffs(0, 104)
                .addBox(slitHalf - 0.40F, slitTop, -11F, 0.40F, 0.35F, 0.72F), PartPose.ZERO);
        body.addOrReplaceChild("eye_bevel_left_bottom", CubeListBuilder.create().texOffs(0, 104)
                .addBox(-slitHalf, slitBottom - 0.35F, -11F, 0.40F, 0.35F, 0.72F), PartPose.ZERO);
        body.addOrReplaceChild("eye_bevel_right_bottom", CubeListBuilder.create().texOffs(0, 104)
                .addBox(slitHalf - 0.40F, slitBottom - 0.35F, -11F, 0.40F, 0.35F, 0.72F), PartPose.ZERO);

        // Separate panels for the two long sides, so each face is subtly shaded.
        PartDefinition sideShade = root.addOrReplaceChild("side_shade", CubeListBuilder.create(), PartPose.ZERO);
        sideShade.addOrReplaceChild("left_wall", CubeListBuilder.create().texOffs(48, 25)
                .addBox(-9F, -4.28F, -11F, 0.72F, 20.28F, 22F), PartPose.ZERO);
        sideShade.addOrReplaceChild("right_wall", CubeListBuilder.create().texOffs(48, 25)
                .addBox(8.28F, -4.28F, -11F, 0.72F, 20.28F, 22F), PartPose.ZERO);

        // 0.7px thick, 10px long physical bottom flaps, outward at ~35°.
        // At rest the four bottom flap tips end at Y~24 (the ground).
        PartDefinition folded = root.addOrReplaceChild("folded_edges", CubeListBuilder.create(), PartPose.ZERO);
        folded.addOrReplaceChild("front_flap", CubeListBuilder.create().texOffs(0, 90)
                .addBox(-9F, 0F, -0.35F, 18F, 10F, 0.70F),
                PartPose.offsetAndRotation(0F, 16F, -11F, -0.62F, 0F, 0F));
        folded.addOrReplaceChild("rear_flap", CubeListBuilder.create().texOffs(0, 90)
                .addBox(-9F, 0F, -0.35F, 18F, 10F, 0.70F),
                PartPose.offsetAndRotation(0F, 16F, 11F, 0.62F, 0F, 0F));
        folded.addOrReplaceChild("left_flap", CubeListBuilder.create().texOffs(45, 84)
                .addBox(-0.35F, 0F, -11F, 0.70F, 10F, 22F),
                PartPose.offsetAndRotation(-9F, 16F, 0F, 0F, 0F, 0.62F));
        folded.addOrReplaceChild("right_flap", CubeListBuilder.create().texOffs(45, 84)
                .addBox(-0.35F, 0F, -11F, 0.70F, 10F, 22F),
                PartPose.offsetAndRotation(9F, 16F, 0F, 0F, 0F, -0.62F));

        // Recessed opaque lining immediately INSIDE the lower mouth of the
        // box, above the four hinged flaps (NOT part of those flaps).
        // The body walls stop at Y=16; this 0.46px-thick dark false bottom
        // occupies Y=15.40..15.86, hiding upper legs, skin and torso from a
        // viewer looking upward without blocking the lower legs below it.
        // Tiny intersections with the inner wall avoid seams/light leaks.
        PartDefinition underside = root.addOrReplaceChild("underside_shade",
                CubeListBuilder.create(), PartPose.ZERO);
        underside.addOrReplaceChild("dark_inner_mouth", CubeListBuilder.create().texOffs(0, 105)
                .addBox(-BoxModelGeometry.LINER_HALF_WIDTH,
                        BoxModelGeometry.LINER_TOP_Y,
                        -BoxModelGeometry.LINER_HALF_DEPTH,
                        2F * BoxModelGeometry.LINER_HALF_WIDTH,
                        BoxModelGeometry.LINER_THICKNESS,
                        2F * BoxModelGeometry.LINER_HALF_DEPTH), PartPose.ZERO);

        // Color-matched low-profile fold scores and corrugated panel edging.
        PartDefinition ink = root.addOrReplaceChild("shadow_ink", CubeListBuilder.create(), PartPose.ZERO);
        ink.addOrReplaceChild("front_left_fold", CubeListBuilder.create().texOffs(90, 0)
                .addBox(-9.02F, -4.1F, -11.08F, 0.22F, 20F, 0.14F), PartPose.ZERO);
        ink.addOrReplaceChild("front_right_fold", CubeListBuilder.create().texOffs(90, 0)
                .addBox(8.80F, -4.1F, -11.08F, 0.22F, 20F, 0.14F), PartPose.ZERO);
        ink.addOrReplaceChild("rear_left_fold", CubeListBuilder.create().texOffs(90, 0)
                .addBox(-9.02F, -4.1F, 10.96F, 0.22F, 20F, 0.14F), PartPose.ZERO);
        ink.addOrReplaceChild("rear_right_fold", CubeListBuilder.create().texOffs(90, 0)
                .addBox(8.80F, -4.1F, 10.96F, 0.22F, 20F, 0.14F), PartPose.ZERO);
        ink.addOrReplaceChild("roof_front_crease", CubeListBuilder.create().texOffs(94, 0)
                .addBox(-8.9F, -5.10F, -11F, 17.8F, 0.16F, 0.20F), PartPose.ZERO);
        ink.addOrReplaceChild("roof_back_crease", CubeListBuilder.create().texOffs(94, 0)
                .addBox(-8.9F, -5.10F, 10.80F, 17.8F, 0.16F, 0.20F), PartPose.ZERO);
        ink.addOrReplaceChild("front_hinge_crease", CubeListBuilder.create().texOffs(87, 37)
                .addBox(-8.5F, 15.70F, -11.12F, 17F, 0.19F, 0.14F), PartPose.ZERO);
        ink.addOrReplaceChild("rear_hinge_crease", CubeListBuilder.create().texOffs(87, 37)
                .addBox(-8.5F, 15.70F, 10.98F, 17F, 0.19F, 0.14F), PartPose.ZERO);
        ink.addOrReplaceChild("front_slit_top_rim", CubeListBuilder.create().texOffs(90, 16)
                .addBox(-5.4F, slitTop - 0.20F, -11.10F, 10.8F, 0.16F, 0.12F), PartPose.ZERO);
        ink.addOrReplaceChild("front_slit_bottom_rim", CubeListBuilder.create().texOffs(90, 16)
                .addBox(-5.4F, slitBottom + 0.035F, -11.10F, 10.8F, 0.16F, 0.12F), PartPose.ZERO);
        ink.addOrReplaceChild("front_slit_left_rim", CubeListBuilder.create().texOffs(90, 16)
                .addBox(-5.50F, slitTop, -11.10F, 0.14F, slitBottom - slitTop, 0.12F), PartPose.ZERO);
        ink.addOrReplaceChild("front_slit_right_rim", CubeListBuilder.create().texOffs(90, 16)
                .addBox(5.36F, slitTop, -11.10F, 0.14F, slitBottom - slitTop, 0.12F), PartPose.ZERO);
        // Two shorter rear scores evoke real folded corrugated stock, not metal seams.
        ink.addOrReplaceChild("rear_score_left", CubeListBuilder.create().texOffs(89, 25)
                .addBox(-7.9F, -3.6F, 11.025F, 0.14F, 5.0F, 0.12F), PartPose.ZERO);
        ink.addOrReplaceChild("rear_score_right", CubeListBuilder.create().texOffs(89, 25)
                .addBox(7.76F, -3.6F, 11.025F, 0.14F, 5.0F, 0.12F), PartPose.ZERO);

        // Soft peach-brown paper tape across top, down front and back, with
        // raised 1/8-pixel edges: never dyed (matches the user's original icon).
        PartDefinition tape = root.addOrReplaceChild("tape", CubeListBuilder.create(), PartPose.ZERO);
        tape.addOrReplaceChild("seal_across_top", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-1.55F, -5.19F, -11F, 3.10F, 0.18F, 22F), PartPose.ZERO);
        tape.addOrReplaceChild("seal_front_turn", CubeListBuilder.create().texOffs(0, 29)
                .addBox(-1.55F, -5F, -11.11F, 3.10F, 4.1F, 0.13F), PartPose.ZERO);
        tape.addOrReplaceChild("seal_back_turn", CubeListBuilder.create().texOffs(0, 29)
                .addBox(-1.55F, -5F, 10.99F, 3.10F, 4.1F, 0.13F), PartPose.ZERO);
        tape.addOrReplaceChild("seal_edge_left", CubeListBuilder.create().texOffs(12, 15)
                .addBox(-1.59F, -5.23F, -10.7F, 0.13F, 0.10F, 21.4F), PartPose.ZERO);
        tape.addOrReplaceChild("seal_edge_right", CubeListBuilder.create().texOffs(12, 15)
                .addBox(1.46F, -5.23F, -10.7F, 0.13F, 0.10F, 21.4F), PartPose.ZERO);

        // The box's real front opening is now COMPLETELY OPEN. The previous
        // two opaque recess/head-shadow cuboids filled the hole and hid the
        // player's face. Only four slim rims remain along its edges.
        // Behind them we draw a narrow strip of the actual player's face skin,
        // not a generic black head painted over the viewing slot.
        PartDefinition eye = root.addOrReplaceChild("eye_cavity", CubeListBuilder.create(), PartPose.ZERO);
        eye.addOrReplaceChild("inner_lip_above", CubeListBuilder.create().texOffs(100, 65)
                .addBox(-5.25F, slitTop - 0.22F, -10.45F, 10.5F, 0.20F, 1.6F), PartPose.ZERO);
        eye.addOrReplaceChild("inner_lip_below", CubeListBuilder.create().texOffs(100, 65)
                .addBox(-5.25F, slitBottom + 0.02F, -10.45F, 10.5F, 0.20F, 1.6F), PartPose.ZERO);
        eye.addOrReplaceChild("inner_lip_left", CubeListBuilder.create().texOffs(100, 65)
                .addBox(-5.35F, slitTop, -10.45F, 0.10F, slitBottom - slitTop, 1.6F), PartPose.ZERO);
        eye.addOrReplaceChild("inner_lip_right", CubeListBuilder.create().texOffs(100, 65)
                .addBox(5.25F, slitTop, -10.45F, 0.10F, slitBottom - slitTop, 1.6F), PartPose.ZERO);

        // The rear recycle logo is rendered as a single UV-controlled 2D print
        // instead of a CubeListBuilder cube (which used to crop/tile its logo).
        return LayerDefinition.create(mesh, 128, 128);
    }

    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        int variant = ClientBoxState.variant(player.getId());
        if (variant == 0) return;
        boolean resting = ClientBoxState.rested(player.getId());
        float lift = BoxModelPose.liftPixels(resting);
        float flap = BoxModelPose.flapAngle(resting);
        foldedEdges.getChild("front_flap").xRot = -flap;
        foldedEdges.getChild("rear_flap").xRot = flap;
        foldedEdges.getChild("left_flap").zRot = flap;
        foldedEdges.getChild("right_flap").zRot = -flap;
        int dye = BoxColors.rgb(variant);
        var paperBuffer = buffers.getBuffer(RenderType.entityCutoutNoCull(PAPER));

        pose.pushPose();
        // PlayerRenderer has already rotated to body yaw. Correct only the
        // difference, so the box front follows the player's actual look yaw.
        float bodyYaw = Mth.rotLerp(partialTick, player.yBodyRotO, player.yBodyRot);
        float headYaw = Mth.rotLerp(partialTick, player.yHeadRotO, player.yHeadRot);
        pose.mulPose(Axis.YP.rotationDegrees(BoxModelPose.lookRelativeYaw(bodyYaw, headYaw)));
        // Local model Y is inverted by the vanilla LivingEntityRenderer setup:
        // +8px lower at rest brings the body hinge to the ground (Y24).
        pose.translate(0D, -lift / 16D, 0D);

        body.render(pose, paperBuffer, light, OverlayTexture.NO_OVERLAY,
                BoxModelGeometry.multiplyTint(dye, 1.0F));
        sideShade.render(pose, paperBuffer, light, OverlayTexture.NO_OVERLAY,
                BoxModelGeometry.multiplyTint(dye, 0.88F));
        foldedEdges.render(pose, paperBuffer, light, OverlayTexture.NO_OVERLAY,
                BoxModelGeometry.multiplyTint(dye, 0.95F));
        // The liner is independent of dye color: it represents the darkness
        // inside a box, not another dyed outer panel. Fully opaque; no alpha.
        undersideShade.render(pose, paperBuffer, light, OverlayTexture.NO_OVERLAY,
                BoxModelGeometry.UNDERMOUTH_DARK_ARGB);
        shadowInk.render(pose, paperBuffer, light, OverlayTexture.NO_OVERLAY,
                BoxModelGeometry.multiplyTint(dye, 0.58F));
        eyeCavity.render(pose, paperBuffer, light, OverlayTexture.NO_OVERLAY,
                BoxModelGeometry.multiplyTint(dye, 0.42F));
        // The actual player skin is visible as a dim strip behind the slit.
        // The entire player head remains hidden behind the roof and wall.
        renderFaceBehindSlit(player, pose, buffers, light);
        tape.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TAPE)),
                light, OverlayTexture.NO_OVERLAY);
        renderBackRecyclingStamp(pose, buffers, light);
        pose.popPose();
    }

    /**
     * Draw a small strip of the owner's own face (eyes/bridge-of-nose area).
     * The front wall is at Z=-11px, the face is recessed at Z=-8.2px. There
     * are no opaque face/backing panels between the slit and this texture.
     * The black floor lining sits far below at Y=15px and cannot cover it.
     */
    private static void renderFaceBehindSlit(AbstractClientPlayer player, PoseStack pose,
                                             MultiBufferSource buffers, int light) {
        VertexConsumer out = buffers.getBuffer(RenderType.entityCutoutNoCull(player.getSkin().texture()));
        PoseStack.Pose transform = pose.last();
        float left=-3.2F/16.0F, right=3.2F/16.0F;
        float top=-0.68F/16.0F, bottom=0.56F/16.0F, z=-8.2F/16.0F;
        // Player skin's main front face occupies UV 8..16,8..16 on 64px.
        // Sampling y=11..13 retains the eyes for most player skins.
        facePixel(out,transform,left,bottom,z,8F/64F,13F/64F,light);
        facePixel(out,transform,right,bottom,z,16F/64F,13F/64F,light);
        facePixel(out,transform,right,top,z,16F/64F,11F/64F,light);
        facePixel(out,transform,left,top,z,8F/64F,11F/64F,light);
    }

    private static void facePixel(VertexConsumer out, PoseStack.Pose transform,
                                  float x, float y, float z, float u, float v, int light) {
        out.addVertex(transform,x,y,z)
                .setColor(150,140,125,255)
                .setUv(u,v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(transform,0F,0F,-1F);
    }

    /**
     * True transparent UV-mapped printed glyph, facing outside the back (+Z).
     * The standard entity renderer mirrors model X. On the +Z back face,
     * negative model X therefore displays at the rear viewer's RIGHT.
     * The print must remain opposite the -Z eye slit.
     */
    private static void renderBackRecyclingStamp(PoseStack pose, MultiBufferSource buffers, int light) {
        VertexConsumer out = buffers.getBuffer(RenderType.entityCutoutNoCull(RECYCLE));
        PoseStack.Pose transform = pose.last();
        float left = BoxModelGeometry.REAR_STAMP_LEFT_X / 16.0F;
        float right = BoxModelGeometry.REAR_STAMP_RIGHT_X / 16.0F;
        float top = 7.8F / 16.0F, bottom = 14.0F / 16.0F;
        float z = 11.15F / 16.0F;
        addStampVertex(out, transform, left, bottom, z, 0F, 0.25F, light);
        addStampVertex(out, transform, right, bottom, z, 0.25F, 0.25F, light);
        addStampVertex(out, transform, right, top, z, 0.25F, 0F, light);
        addStampVertex(out, transform, left, top, z, 0F, 0F, light);
    }

    private static void addStampVertex(VertexConsumer out, PoseStack.Pose transform,
                                       float x, float y, float z, float u, float v, int light) {
        out.addVertex(transform, x, y, z)
                .setColor(255, 255, 255, 255)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(light)
                .setNormal(transform, 0F, 0F, 1F);
    }
}

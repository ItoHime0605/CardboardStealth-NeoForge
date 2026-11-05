package tw.zonezero.cardboardstealth.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.PartDefinition;
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
import tw.zonezero.cardboardstealth.CardboardStealth;

public final class CardboardLayer extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
    private static final ResourceLocation[] TEXTURES = {
        ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID, "textures/entity/cardboard_box.png"),
        ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID, "textures/entity/delivery_box.png"),
        ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID, "textures/entity/supply_box.png")
    };
    private final ModelPart box;
    public CardboardLayer(PlayerRenderer parent, ModelPart root) {
        super(parent);
        this.box = root.getChild("box");
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        // Temporary geometry: width 18px=1.125 blocks, depth 22px=1.375 blocks.
        // The centre of the underside is completely open; four folded-out
        // bottom flaps surround the exposed feet.
        PartDefinition casing = root.addOrReplaceChild("box", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-9.0F, -5.0F, -11.0F, 18.0F, 21.0F, 22.0F), PartPose.ZERO);
        casing.addOrReplaceChild("front_flap", CubeListBuilder.create().texOffs(0, 35)
                .addBox(-9.0F, 0.0F, -0.5F, 18.0F, 9.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 16.0F, -11.0F, -0.34F, 0, 0));
        casing.addOrReplaceChild("back_flap", CubeListBuilder.create().texOffs(0, 47)
                .addBox(-9.0F, 0.0F, -0.5F, 18.0F, 9.0F, 1.0F),
                PartPose.offsetAndRotation(0.0F, 16.0F, 11.0F, 0.34F, 0, 0));
        casing.addOrReplaceChild("left_flap", CubeListBuilder.create().texOffs(41, 35)
                .addBox(-0.5F, 0.0F, -11.0F, 1.0F, 9.0F, 22.0F),
                PartPose.offsetAndRotation(-9.0F, 16.0F, 0.0F, 0, 0, 0.34F));
        casing.addOrReplaceChild("right_flap", CubeListBuilder.create().texOffs(41, 47)
                .addBox(-0.5F, 0.0F, -11.0F, 1.0F, 9.0F, 22.0F),
                PartPose.offsetAndRotation(9.0F, 16.0F, 0.0F, 0, 0, -0.34F));
        return LayerDefinition.create(mesh, 128, 64);
    }
    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        int type = ClientBoxState.variant(player.getId());
        if (type <= 0 || type > TEXTURES.length) return;
        // For walking, lift ~3 model pixels and expose the player's feet.
        // After exactly five unmoving server ticks, lower the four flap edges
        // to the floor and hide the feet in ClientInit.
        box.y = ClientBoxState.rested(player.getId()) ? 0.0F : -3.0F;
        box.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURES[type - 1])),
                light, OverlayTexture.NO_OVERLAY);
    }
}

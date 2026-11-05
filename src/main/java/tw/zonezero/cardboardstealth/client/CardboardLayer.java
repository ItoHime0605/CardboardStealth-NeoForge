package tw.zonezero.cardboardstealth.client;

import com.mojang.blaze3d.vertex.PoseStack;
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
        // Pixel units: from head to lower calf, leaving last 5 pixels of the legs exposed.
        root.addOrReplaceChild("box", CubeListBuilder.create().texOffs(0, 0)
                .addBox(-9.0F, -8.0F, -9.0F, 18.0F, 27.0F, 18.0F), PartPose.ZERO);
        return LayerDefinition.create(mesh, 128, 64);
    }
    @Override
    public void render(PoseStack pose, MultiBufferSource buffers, int light, AbstractClientPlayer player,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        int type = ClientBoxState.variant(player.getId());
        if (type <= 0 || type > TEXTURES.length) return;
        box.render(pose, buffers.getBuffer(RenderType.entityCutoutNoCull(TEXTURES[type - 1])),
                light, OverlayTexture.NO_OVERLAY);
    }
}

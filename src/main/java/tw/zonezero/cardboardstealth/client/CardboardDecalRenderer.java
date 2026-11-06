package tw.zonezero.cardboardstealth.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import tw.zonezero.cardboardstealth.BoxModelGeometry;
import tw.zonezero.cardboardstealth.CardboardStealth;
import tw.zonezero.cardboardstealth.DecalStackRules;

/**
 * Surface decal renderer: nine curated cardboard anchors, all on exterior
 * walls or roof. No decals may touch the peep slit or rear recycle icon.
 *
 * Layers are drawn in acquisition order, from old to new. Each later decal
 * sits 0.035 model pixels farther out so it *visibly covers* an earlier
 * sticker when the same location is used. It is not a flat additive blend.
 */
public final class CardboardDecalRenderer {
    private CardboardDecalRenderer() {}

    public static final ResourceLocation WHITE_ATLAS =
            ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID,
                    "textures/decals/white_atlas.png");
    public static final ResourceLocation LIGHT_GRAY_ATLAS =
            ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID,
                    "textures/decals/light_gray_atlas.png");

    public static final ResourceLocation GRAY_ATLAS =
            ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID,
                    "textures/decals/gray_atlas.png");

    public static final ResourceLocation BLACK_ATLAS =
            ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID,
                    "textures/decals/black_atlas.png");

    public static final ResourceLocation RED_ATLAS =
            ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID,
                    "textures/decals/red_atlas.png");

    public static final ResourceLocation ORANGE_ATLAS =
            ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID,
                    "textures/decals/orange_atlas.png");

    public static final ResourceLocation YELLOW_ATLAS =
            ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID,
                    "textures/decals/yellow_atlas.png");

    public static final ResourceLocation LIME_ATLAS =
            ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID,
                    "textures/decals/lime_atlas.png");

    public static final ResourceLocation PINK_ATLAS =
            ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID,
                    "textures/decals/pink_atlas.png");

    public static final ResourceLocation BROWN_ATLAS =
            ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID,
                    "textures/decals/brown_atlas.png");

    public static final ResourceLocation GREEN_ATLAS =
            ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID,
                    "textures/decals/green_atlas.png");

    public static final ResourceLocation CYAN_ATLAS =
            ResourceLocation.fromNamespaceAndPath(CardboardStealth.MOD_ID,
                    "textures/decals/cyan_atlas.png");

    /** Finished art families only; unfinished carpets must never render blank. */
    public static ResourceLocation atlasFor(int carpetColor) {
        return switch (carpetColor) {
            case DecalStackRules.WHITE -> WHITE_ATLAS;
            case DecalStackRules.LIGHT_GRAY -> LIGHT_GRAY_ATLAS;
            case DecalStackRules.GRAY -> GRAY_ATLAS;
            case DecalStackRules.BLACK -> BLACK_ATLAS;
            case DecalStackRules.RED -> RED_ATLAS;
            case DecalStackRules.ORANGE -> ORANGE_ATLAS;
            case DecalStackRules.YELLOW -> YELLOW_ATLAS;
            case DecalStackRules.LIME -> LIME_ATLAS;
            case DecalStackRules.PINK -> PINK_ATLAS;
            case DecalStackRules.BROWN -> BROWN_ATLAS;
            case DecalStackRules.GREEN -> GREEN_ATLAS;
            case DecalStackRules.CYAN -> CYAN_ATLAS;
            default -> null;
        };
    }

    private static final int[] FACES = {0,0,1,1,2,2,3,3,4};
    private static final float[] CENTER_A = {-4.4F,4.4F,-4.4F,4.4F,-4.4F,4.4F,-3.6F,3.6F,-4.5F};
    private static final float[] CENTER_Y = {8.4F,8.4F,3.6F,10F,3.6F,10F,3.1F,3.1F,-5.23F};
    private static final float[] CENTER_Z = {0F,0F,-3.5F,3.5F,-3.5F,3.5F,0F,0F,0F};

    public static void render(PoseStack pose, MultiBufferSource buffers, int light, int[] layers) {
        int[] ordered = DecalStackRules.sanitized(layers);
        if (ordered.length == 0) return;
        for (int index = 0; index < ordered.length; index++) {
            int packed = ordered[index];
            ResourceLocation atlas = atlasFor(DecalStackRules.color(packed));
            if (atlas == null) continue;
            // The global layer index sets depth; all carpet colors
            // retain exact oldest-to-newest order on a shared anchor.
            VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutoutNoCull(atlas));
            drawDecal(pose, buffer, light, DecalStackRules.anchor(packed),
                    DecalStackRules.art(packed), index);
        }
    }

    private static void drawDecal(PoseStack pose, VertexConsumer out, int light,
                                  int anchor, int art, int layer) {
        if (anchor < 0 || anchor >= FACES.length || art < 0 || art >= 6) return;
        float offset = 0.16F + layer * 0.035F;
        float x=CENTER_A[anchor], y=CENTER_Y[anchor], z=CENTER_Z[anchor];
        int face=FACES[anchor];
        // Roof decal stays beside, NEVER over, the central 1.4px tape strip.
        final float halfWidth = anchor == 8 ? 3.0F : 4.15F;
        final float halfHeight = anchor == 8 ? 3.5F : 2.65F;
        final float tilt = (art%3-1)*0.065F;
        float sn=(float)Math.sin(tilt),cs=(float)Math.cos(tilt);
        float u0=(art%2)*0.5F, u1=u0+0.5F;
        float v0=(art/2)/3F, v1=v0+1F/3F;
        float[] cornersX = {-halfWidth,halfWidth,halfWidth,-halfWidth};
        float[] cornersY = {halfHeight,halfHeight,-halfHeight,-halfHeight};
        float[] us = {u0,u1,u1,u0};
        float[] vs = {v1,v1,v0,v0};
        PoseStack.Pose last = pose.last();
        for (int i=0;i<4;i++) {
            float a=cornersX[i]*cs-cornersY[i]*sn;
            float b=cornersX[i]*sn+cornersY[i]*cs;
            float px,py,pz,nx=0,ny=0,nz=0;
            switch(face) {
                case 0 -> { // FRONT (-Z), below the peep slot
                    px=x+a;py=y+b;pz=BoxModelGeometry.FRONT_Z-offset;nz=-1F;
                }
                case 1 -> { // LEFT (-X)
                    px=-9F-offset;py=y+b;pz=z+a;nx=-1F;
                }
                case 2 -> { // RIGHT (+X)
                    px=9F+offset;py=y+b;pz=z-a;nx=1F;
                }
                case 3 -> { // BACK (+Z), upper region away from recycle logo
                    px=x-a;py=y+b;pz=BoxModelGeometry.BACK_Z+offset;nz=1F;
                }
                default -> { // ROOF, not touching the taped central ridge
                    px=x+a;py=-5F-offset;pz=b;ny=-1F;
                }
            }
            out.addVertex(last,px/16F,py/16F,pz/16F)
                    .setColor(255,255,255,255)
                    .setUv(us[i],vs[i])
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(last,nx,ny,nz);
        }
    }
}

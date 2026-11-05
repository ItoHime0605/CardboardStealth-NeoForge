import tw.zonezero.cardboardstealth.BoxModelGeometry;
import tw.zonezero.cardboardstealth.BoxViewGeometry;

public final class BoxModelGeometryTest {
    private static int checked;
    private static void check(boolean ok, String label) {
        checked++;
        if (!ok) throw new AssertionError(label);
    }
    public static void main(String[] args) {
        check(BoxModelGeometry.WIDTH_PIXELS == 18F, "box width 1.125 blocks");
        check(BoxModelGeometry.DEPTH_PIXELS == 22F, "box length 1.375 blocks");
        check(BoxModelGeometry.HEIGHT_PIXELS == 21F, "body height");
        check(BoxModelGeometry.FRONT_Z == -11F, "eye facing forward");
        check(BoxModelGeometry.BACK_Z == 11F, "recycling stamp on rear");
        check(BoxModelGeometry.slitWidth() > 9.5F, "peep hole is usable from outside");
        check(BoxModelGeometry.slitWidth() < 11F, "peep hole does not span entire front");
        check(BoxModelGeometry.slitHeight() == 1.5F, "exterior slot is slender");
        check(BoxModelGeometry.externalSlitAreaRatio() < 0.045F, "exterior opening has small area");
        check(BoxViewGeometry.calculate(757, 496).bottom() - BoxViewGeometry.calculate(757, 496).top() > 120,
                "first-person HUD opening intentionally much taller");
        check(BoxModelGeometry.PANEL_THICKNESS < 1F, "cardboard walls are thin, not solid cube");
        check(BoxModelGeometry.multiplyTint(0xFFFFFF,1F) == 0xFFFFFFFF,"neutral tint preserved");
        check(BoxModelGeometry.multiplyTint(0xAA7840,0.5F) == 0xFF553C20,"shadow multiplies dye safely");
        check(BoxModelGeometry.multiplyTint(0x000000,0.5F) == 0xFF000000,"zero color remains zero");
        check(BoxModelGeometry.multiplyTint(0xFFFFFF,2F) == 0xFFFFFFFF,"tint clamped");
        check(BoxModelGeometry.linerCoverage() > 0.85F, "opaque interior spans most of box mouth");
        check(BoxModelGeometry.LINER_HALF_WIDTH < BoxModelGeometry.WIDTH_PIXELS / 2F,
                "dark interior hidden behind side walls");
        check(BoxModelGeometry.LINER_HALF_DEPTH < BoxModelGeometry.DEPTH_PIXELS / 2F,
                "dark interior hidden behind front and rear");
        check(BoxModelGeometry.LINER_TOP_Y + BoxModelGeometry.LINER_THICKNESS
                < BoxModelGeometry.FLOOR_BODY_Y, "dark lining recessed above flap hinges");
        check((BoxModelGeometry.UNDERMOUTH_DARK_ARGB >>> 24) == 255,
                "underside is fully opaque, not transparent");
        check(BoxModelGeometry.FLOOR_BODY_Y - tw.zonezero.cardboardstealth.BoxModelPose.liftPixels(true)
                == 24F, "rested box wall hinge reaches ground plane");
        check(Math.abs(10D * Math.cos(tw.zonezero.cardboardstealth.BoxModelPose.flapAngle(true))) < 0.00001D,
                "resting flap tips reach the ground without drooping below it");
        System.out.println("PASS: " + checked + "/22 physical box and ground-contact checks");
    }
}

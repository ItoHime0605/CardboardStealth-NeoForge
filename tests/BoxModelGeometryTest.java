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
        System.out.println("PASS: " + checked + "/15 physical cardboard geometry and independent eye mask checks");
    }
}

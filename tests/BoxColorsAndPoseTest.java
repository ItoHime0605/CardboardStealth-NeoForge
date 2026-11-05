import tw.zonezero.cardboardstealth.BoxColors;
import tw.zonezero.cardboardstealth.BoxModelPose;

public final class BoxColorsAndPoseTest {
    private static int checks;
    private static void check(boolean ok, String description) {
        checks++;
        if (!ok) throw new AssertionError(description);
    }
    public static void main(String[] args) {
        check(BoxColors.NAMES.length == 16, "16 Minecraft dye colors");
        check(BoxColors.dyedVariant(-1) == 0, "negative color rejected");
        check(BoxColors.dyedVariant(16) == 0, "out of range rejected");
        check(BoxColors.dyedVariant(0) == 4, "first dyed variant");
        check(BoxColors.dyedVariant(15) == 19, "last dyed variant");
        for (int i = 0; i < 16; i++) {
            check(!BoxColors.colorName(4 + i).isBlank(), "name for color " + i);
            check(BoxColors.rgb(4 + i) != 0, "tint for color " + i);
        }
        check(BoxColors.rgb(1) != 0, "legacy box still colored");
        check(BoxColors.rgb(2) != 0 && BoxColors.rgb(3) != 0, "legacy red/green");
        check(BoxModelPose.liftPixels(false) == 3F, "walking reveals legs");
        check(BoxModelPose.liftPixels(true) == -8F, "resting body hinge reaches ground");
        check(BoxModelPose.flapAngle(false) > 0.60F, "walking flaps angled");
        check(Math.abs(Math.cos(BoxModelPose.flapAngle(true))) < 0.000001D,
                "resting flaps are exactly horizontal");
        check(BoxModelPose.lookRelativeYaw(30F, 90F) == -60F, "box follows head yaw");
        check(BoxModelPose.lookRelativeYaw(170F, -170F) == -20F, "yaw wraps correctly");
        check(BoxModelPose.lookRelativeYaw(45F, 45F) == 0F, "no unnecessary yaw");
        System.out.println("PASS: " + checks + "/46 dye palette and pose checks");
    }
}

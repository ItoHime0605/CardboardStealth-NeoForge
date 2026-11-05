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
        check(BoxModelPose.liftPixels(true) == 0F, "resting lowers to ground");
        check(BoxModelPose.stepBackTilt(0.2, true, false) < 0F, "steps tilt back");
        check(BoxModelPose.stepBackTilt(0.2, false, false) == 0F, "jump does not tilt");
        check(BoxModelPose.stepBackTilt(0.2, true, true) == 0F, "settled no tilt");
        check(BoxModelPose.stepBackTilt(0.8, true, false) == 0F, "large jumps ignored");
        System.out.println("PASS: " + checks + "/44 dye palette and model animation checks");
    }
}

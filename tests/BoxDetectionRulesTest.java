import tw.zonezero.cardboardstealth.BoxDetectionRules;

/** Pure-Java boundary regression, runs with JDK 21 independently of NeoForge. */
public final class BoxDetectionRulesTest {
    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) {
        check(BoxDetectionRules.recognitionRadius(false) == 1.25D, "stationary radius");
        check(BoxDetectionRules.recognitionRadius(true) == 3.50D, "moving radius");
        check(!BoxDetectionRules.concealsNewTarget(1.25D * 1.25D, false), "stationary boundary detectable");
        check(BoxDetectionRules.concealsNewTarget(1.26D * 1.26D, false), "stationary beyond radius concealed");
        check(!BoxDetectionRules.concealsNewTarget(3.50D * 3.50D, true), "moving boundary detectable");
        check(BoxDetectionRules.concealsNewTarget(3.51D * 3.51D, true), "moving beyond radius concealed");
        check(BoxDetectionRules.concealsNewTarget(2.0D * 2.0D, false), "2 blocks stationary concealed");
        check(!BoxDetectionRules.concealsNewTarget(2.0D * 2.0D, true), "2 blocks moving detected");
        check(!BoxDetectionRules.concealsNewTarget(Double.NaN, false), "invalid distance safe default");
        System.out.println("PASS: 9/9 detection boundary checks");
    }
}

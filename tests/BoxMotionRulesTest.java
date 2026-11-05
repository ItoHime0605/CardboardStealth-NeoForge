import tw.zonezero.cardboardstealth.BoxMotionRules;

/** Verify exact tick boundaries, especially the five-tick stationary settling. */
public final class BoxMotionRulesTest {
    private static int count;
    private static void check(boolean result, String label) {
        count++;
        if (!result) throw new AssertionError(label);
    }
    public static void main(String[] args) {
        check(BoxMotionRules.SETTLE_TICKS == 5, "five ticks");
        long until = BoxMotionRules.movingUntil(100L);
        check(until == 104L, "last moving tick");
        check(BoxMotionRules.countsAsMoving(100L, until), "motion tick");
        check(BoxMotionRules.countsAsMoving(101L, until), "first still tick");
        check(BoxMotionRules.countsAsMoving(102L, until), "second still tick");
        check(BoxMotionRules.countsAsMoving(103L, until), "third still tick");
        check(BoxMotionRules.countsAsMoving(104L, until), "fourth still tick");
        check(!BoxMotionRules.countsAsMoving(105L, until), "fifth still tick settles");
        check(BoxMotionRules.countsAsMoving(106L, BoxMotionRules.movingUntil(106L)), "moving lifts immediately");
        check(!BoxMotionRules.moved(0.0, 0.0, 0.0, true), "grounded immobile");
        check(!BoxMotionRules.moved(0.009, 0.0, 0.0, true), "sub-pixel jitter");
        check(BoxMotionRules.moved(0.02, 0.0, 0.0, true), "walk counts");
        check(BoxMotionRules.moved(0.0, 0.0, 0.0, false), "airborne counts");
        System.out.println("PASS: " + count + "/13 cardboard resting-motion checks");
    }
}

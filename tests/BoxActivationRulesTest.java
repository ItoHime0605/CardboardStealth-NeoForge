import tw.zonezero.cardboardstealth.BoxActivationRules;

/** Tests ground, timer and active pursuit rules without a Minecraft runtime. */
public final class BoxActivationRulesTest {
    private static int checks;
    private static void check(boolean value, String description) {
        checks++;
        if (!value) throw new AssertionError(description);
    }
    public static void main(String[] args) {
        check(BoxActivationRules.REQUIRED_CROUCH_TICKS == 20, "one-second hold");
        check(BoxActivationRules.advanceHold(0, true) == 1, "first grounded tick");
        check(BoxActivationRules.advanceHold(18, true) == 19, "nineteenth tick");
        check(BoxActivationRules.advanceHold(19, true) == 20, "twentieth tick");
        check(BoxActivationRules.advanceHold(20, true) == 20, "hold saturates");
        check(BoxActivationRules.advanceHold(19, false) == 0, "jump restarts hold");
        check(BoxActivationRules.advanceHold(-10, true) == 1, "corrupt negative count safe");
        check(!BoxActivationRules.mayActivate(19, false), "too early cannot activate");
        check(BoxActivationRules.mayActivate(20, false), "safe after full hold");
        check(!BoxActivationRules.mayActivate(20, true), "pursued player cannot activate");
        check(!BoxActivationRules.mayActivate(100, true), "extra waiting cannot erase pursuit");
        System.out.println("PASS: " + checks + "/11 cardboard activation checks");
    }
}

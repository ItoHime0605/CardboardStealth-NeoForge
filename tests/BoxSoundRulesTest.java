import tw.zonezero.cardboardstealth.BoxSoundRules;

/** JDK-only regression test for player / block footsteps and landing sounds. */
public final class BoxSoundRulesTest {
    private static int checks;
    private static void check(boolean condition, String name) {
        checks++;
        if (!condition) throw new AssertionError(name);
    }
    public static void main(String[] args) {
        check(BoxSoundRules.isStep("block.grass.step"), "grass step");
        check(BoxSoundRules.isStep("block.stone.step"), "stone step");
        check(BoxSoundRules.isStep("entity.player.sneak_step"), "sneak step");
        check(BoxSoundRules.isLanding("block.grass.fall"), "grass landing");
        check(BoxSoundRules.isLanding("entity.player.small_fall"), "small fall");
        check(BoxSoundRules.isLanding("entity.player.big_fall"), "big fall");
        check(!BoxSoundRules.isStep("entity.player.small_fall"), "fall not step");
        check(!BoxSoundRules.isLanding("block.stone.step"), "step not fall");
        check(!BoxSoundRules.isStep("tacz.gun_fire"), "weapon not footstep");
        check(!BoxSoundRules.isLanding("tacz.gun_fire"), "weapon not landing");
        check(!BoxSoundRules.isStep(null), "null safe step");
        check(!BoxSoundRules.isLanding(null), "null safe fall");
        System.out.println("PASS: " + checks + "/12 sound classification checks");
    }
}

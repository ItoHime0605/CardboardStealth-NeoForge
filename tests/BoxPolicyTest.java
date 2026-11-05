import tw.zonezero.cardboardstealth.BoxPolicy;

public final class BoxPolicyTest {
    private static int n;
    private static void check(boolean pass, String label) {
        n++;
        if (!pass) throw new AssertionError(label);
    }
    public static void main(String[] args) {
        check(BoxPolicy.isPistolCategory("pistol"), "pistol");
        check(BoxPolicy.isPistolCategory("PISTOL"), "uppercase");
        check(BoxPolicy.isPistolCategory(" pistol "), "whitespace");
        check(BoxPolicy.isPistolCategory("handgun"), "handgun");
        check(BoxPolicy.isPistolCategory("revolver"), "revolver");
        check(!BoxPolicy.isPistolCategory("smg"), "submachine gun prohibited");
        check(!BoxPolicy.isPistolCategory("rifle"), "rifle prohibited");
        check(!BoxPolicy.isPistolCategory("sniper"), "sniper prohibited");
        check(!BoxPolicy.isPistolCategory("rpg"), "rpg prohibited");
        check(!BoxPolicy.isPistolCategory("grenade_launcher"), "grenade launcher prohibited");
        check(!BoxPolicy.isPistolCategory("shotgun"), "shotgun prohibited");
        check(!BoxPolicy.isPistolCategory("machine_pistol"), "machine pistol not automatically allowed");
        check(!BoxPolicy.isPistolCategory(null), "missing category fails closed");
        check(!BoxPolicy.isPistolCategory("unknown"), "unrecognized type fails closed");
        check(BoxPolicy.gunshotSenseStrength(32, false) == 32, "normal pistol noise");
        check(BoxPolicy.gunshotSenseStrength(32, true) == 8, "suppressed sound");
        check(BoxPolicy.gunshotSenseStrength(1, true) == 1, "suppressed is never completely silent");
        check(BoxPolicy.gunshotSenseStrength(0, false) == 1, "invalid strength clamped");
        System.out.println("PASS: " + n + "/18 gun policy checks");
    }
}

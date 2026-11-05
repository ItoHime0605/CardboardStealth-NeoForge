import tw.zonezero.cardboardstealth.BoxRenderRules;

public final class BoxRenderRulesTest {
    private static int checks;
    private static void check(boolean pass, String label) {
        checks++;
        if (!pass) throw new AssertionError(label);
    }
    public static void main(String[] args) {
        check(BoxRenderRules.renderAttachedLayer(false, false), "normal hand item visible");
        check(BoxRenderRules.renderAttachedLayer(false, true), "normal cardboard layer is allowed");
        check(!BoxRenderRules.renderAttachedLayer(true, false), "boxed hand item hidden");
        check(BoxRenderRules.renderAttachedLayer(true, true), "boxed cardboard visible");
        check(!BoxRenderRules.renderAttachedLayer(true, false), "boxed armor hidden");
        check(!BoxRenderRules.renderAttachedLayer(true, false), "boxed wings hidden");
        check(!BoxRenderRules.renderAttachedLayer(true, false), "boxed cape hidden");
        check(!BoxRenderRules.renderAttachedLayer(true, false), "boxed Curios accessory hidden");
        check(BoxRenderRules.showPlayerNameplate(false), "normally visible nameplate");
        check(!BoxRenderRules.showPlayerNameplate(true), "boxed player nameplate hidden");
        System.out.println("PASS: " + checks + "/10 cardboard render and nameplate rules");
    }
}

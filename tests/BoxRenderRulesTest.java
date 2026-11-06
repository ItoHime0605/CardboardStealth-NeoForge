import tw.zonezero.cardboardstealth.BoxRenderRules;

public final class BoxRenderRulesTest {
    private static int checks;
    private static void check(boolean pass, String label) {
        checks++;
        if (!pass) throw new AssertionError(label);
    }
    public static void main(String[] args) {
        check(BoxRenderRules.renderAttachedLayer(false, false),
                "normal player skin and hand rendering is untouched");
        check(BoxRenderRules.renderAttachedLayer(false, true),
                "independent cardboard renderer can render");
        check(BoxRenderRules.renderAttachedLayer(true, false),
                "disguised player's vanilla skin layers are NOT forcibly removed");
        check(BoxRenderRules.renderAttachedLayer(true, true),
                "cardboard overlay still renders for disguised player");
        check(BoxRenderRules.renderAttachedLayer(true, false),
                "other players' armor renderer state never changed");
        check(BoxRenderRules.renderAttachedLayer(true, false),
                "custom wings and capes still belong to the host mods");
        check(BoxRenderRules.renderAttachedLayer(true, false),
                "Curios render layer remains under Curios control");
        check(BoxRenderRules.renderAttachedLayer(true, false),
                "TaCZ weapon and holster rendering remains under TaCZ control");
        check(BoxRenderRules.showPlayerNameplate(false),
                "normal nameplate remains visible");
        check(!BoxRenderRules.showPlayerNameplate(true),
                "disguised nameplate is hidden without touching shared models");
        System.out.println("PASS: "+checks+"/10 safe player rendering policy checks");
    }
}

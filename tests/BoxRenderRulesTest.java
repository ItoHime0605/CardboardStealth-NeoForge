import tw.zonezero.cardboardstealth.BoxRenderRules;

public final class BoxRenderRulesTest {
    private static int checks;
    private static void check(boolean pass,String label){
        checks++;
        if(!pass)throw new AssertionError(label);
    }
    public static void main(String[] args) {
        check(BoxRenderRules.renderBody(false),"normal SKIN is visible");
        check(!BoxRenderRules.renderBody(true),"boxed SKIN render call is suppressed");
        check(BoxRenderRules.renderAttachedLayer(false,false),"normal Curios and TaCZ layers draw");
        check(BoxRenderRules.renderAttachedLayer(false,true),"normal cardboard layer is allowed");
        check(!BoxRenderRules.renderAttachedLayer(true,false),
                "only current boxed player suppresses foreign layers");
        check(BoxRenderRules.renderAttachedLayer(true,true),
                "the disguised player's own cardboard layer still draws");
        check(BoxRenderRules.showPlayerNameplate(false),"normal nametag visible");
        check(!BoxRenderRules.showPlayerNameplate(true),"boxed nametag hidden");
        // No SAVED renderers, global flags, restore order, or paired callbacks.
        // Every operation recomputes from the argument for that player.
        boolean[] sequence={false,true,true,false,false,true,false};
        for(boolean boxed:sequence) {
            check(BoxRenderRules.renderBody(boxed)==!boxed,
                    "SKIN resumes on the very first unboxed draw invocation");
            check(BoxRenderRules.renderAttachedLayer(boxed,false)==!boxed,
                    "all third-party render layers immediately resume");
            check(BoxRenderRules.renderAttachedLayer(boxed,true),
                    "own cardboard layer always permitted by render policy");
        }
        for(int i=0;i<75;i++) {
            // Simulate distinct players interleaved between disguised draws.
            check(!BoxRenderRules.renderBody(true),"boxed player blocked");
            check(BoxRenderRules.renderBody(false),"second ordinary player unaffected");
            check(BoxRenderRules.renderAttachedLayer(false,false),
                    "a second player's external add-ons are always visible");
        }
        System.out.println("PASS: "+checks+"/"+checks+
                " per-invocation concealment, cross-player isolation and instant restoration checks");
    }
}

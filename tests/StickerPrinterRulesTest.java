import tw.zonezero.cardboardstealth.StickerPrinterRules;

/** Exact costs, all 16 inks, output safety and 96 fixed recipe IDs. */
public final class StickerPrinterRulesTest {
    private static int checks;
    private static void check(boolean condition,String message){
        checks++;
        if(!condition)throw new AssertionError(message);
    }
    public static void main(String[] args) {
        String[] colors={
            "white","light_gray","gray","black","red","orange",
            "yellow","lime","pink","brown","green","cyan",
            "blue","purple","magenta","light_blue"
        };
        check(!StickerPrinterRules.canPrint(1,6,-1,0,true),
              "non-dye item cannot bypass color validation");
        check(!StickerPrinterRules.canPrint(1,6,16,0,true),
              "no seventeenth dye family");
        check(!StickerPrinterRules.canPrint(1,6,0,-1,true),
              "negative design cannot be submitted");
        check(!StickerPrinterRules.canPrint(1,6,0,6,true),
              "missing seventh design must be rejected");
        check(!StickerPrinterRules.canPrint(1,6,0,0,false),
              "printer may not overwrite an uncollected output item");
        check(!StickerPrinterRules.canPrint(0,6,0,0,true),
              "empty blank-paper slot must not print");
        for(int color=0;color<16;color++){
            for(int pattern=0;pattern<6;pattern++){
                int cost=pattern+1;
                check(StickerPrinterRules.requiredDye(pattern)==cost,
                      "exact dye cost equals selected 1-based design");
                check(StickerPrinterRules.canPrint(1,cost,color,pattern,true),
                      "correct dye count and blank paper permit each of 96 designs");
                check(!StickerPrinterRules.canPrint(1,cost-1,color,pattern,true),
                      "one missing dye must prevent printing");
                check(StickerPrinterRules.canPrint(4,64,color,pattern,true),
                      "stacked ingredients allow printing one safe product");
                check(StickerPrinterRules.outputId(colors,color,pattern)
                        .equals("sticker_"+colors[color]+"_"+cost),
                      "machine yields exactly the same registered item as crafting table");
            }
        }
        check(StickerPrinterRules.requiredDye(6)==0,
              "invalid pattern cost cannot be negative or positive");
        System.out.println("PASS: "+checks+"/"+checks+" sticker printer server rules");
    }
}

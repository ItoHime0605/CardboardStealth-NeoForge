import tw.zonezero.cardboardstealth.BoxInteriorPalette;
public final class BoxInteriorPaletteTest {
    private static int checks;
    private static void ok(boolean x) { checks++; if (!x) throw new AssertionError("check "+checks); }
    public static void main(String[] args) {
        ok(BoxInteriorPalette.innerBase(1) != BoxInteriorPalette.innerBase(4));
        ok(BoxInteriorPalette.innerBase(4) != BoxInteriorPalette.innerBase(15));
        ok(BoxInteriorPalette.innerBase(17) != BoxInteriorPalette.innerBase(18));
        ok((BoxInteriorPalette.innerBase(15) >>> 24) == 255);
        for (int v=1; v<=19; v++) {
            int c=BoxInteriorPalette.innerBase(v);
            ok(c != 0);
            ok(BoxInteriorPalette.shade(c,0.8F) != c);
        }
        ok(BoxInteriorPalette.mix(0x000000,0xFFFFFF,0.5F)==0x808080);
        System.out.println("PASS: "+checks+"/43 matte cardboard interior checks");
    }
}

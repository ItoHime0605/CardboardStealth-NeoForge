import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import tw.zonezero.cardboardstealth.BoxLayerMasking;

public final class BoxLayerMaskingTest {
    private static int checks;
    private static void check(boolean ok, String label) {
        checks++;
        if (!ok) throw new AssertionError(label);
    }
    public static void main(String[] args) {
        List<String> layers = new ArrayList<>(Arrays.asList(
            "armor", "held_pistol", "cape", "cardboard", "wings", "curios", "hotbar_gun"));
        List<String> saved = BoxLayerMasking.retainDuringRender(layers, "cardboard"::equals);
        check(layers.equals(List.of("cardboard")), "only the box renders in third person");
        check(saved.size() == 7, "all original layers retained for restoration");
        check(saved.get(1).equals("held_pistol"), "original pistol state preserved");
        check(saved.get(6).equals("hotbar_gun"), "TaCZ extra weapon layer preserved in snapshot");
        BoxLayerMasking.restoreAfterRender(layers, saved);
        check(layers.equals(saved), "every attachment restored in original order");
        check(layers.size() == 7, "all attachments restored");
        List<Integer> otherEntity = new ArrayList<>(List.of(1, 2, 3));
        check(otherEntity.equals(List.of(1,2,3)), "other renderers unaffected");
        List<String> onlyBox = new ArrayList<>(List.of("cardboard"));
        List<String> solo = BoxLayerMasking.retainDuringRender(onlyBox, "cardboard"::equals);
        check(onlyBox.size() == 1, "standalone cardboard kept");
        BoxLayerMasking.restoreAfterRender(onlyBox, solo);
        check(onlyBox.equals(List.of("cardboard")), "standalone cardboard restored");
        List<String> duplicates = new ArrayList<>(List.of("armor", "armor", "cardboard"));
        List<String> original = BoxLayerMasking.retainDuringRender(duplicates, "cardboard"::equals);
        BoxLayerMasking.restoreAfterRender(duplicates, original);
        check(duplicates.equals(List.of("armor", "armor", "cardboard")), "duplicated layers restored");
        System.out.println("PASS: " + checks + "/10 cardboard temporary layer masking checks");
    }
}

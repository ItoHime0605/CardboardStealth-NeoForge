import tw.zonezero.cardboardstealth.BoxViewGeometry;

public final class BoxViewGeometryTest {
    private static int count = 0;
    private static void check(boolean v, String label) {
        count++;
        if (!v) throw new AssertionError(label);
    }

    public static void main(String[] args) {
        BoxViewGeometry.Aperture a = BoxViewGeometry.calculate(757, 496);
        check(Math.abs(a.left() - 211) <= 5, "image left reference");
        check(Math.abs(a.right() - 551) <= 5, "image right reference");
        check(Math.abs(a.top() - 153) <= 3, "image top reference");
        check(Math.abs(a.bottom() - 307) <= 3, "image bottom reference");
        check(a.left() + a.right() >= 755 && a.left() + a.right() <= 759, "horizontal centre");
        check(a.insetAt(a.top()) == a.radiusX(), "top corner rounds inwards");
        check(a.insetAt(a.bottom() - 1) == a.radiusX(), "bottom corner rounds inwards");
        check(a.insetAt((a.top() + a.bottom()) / 2) == 0, "middle edge is straight");
        check(a.insetAt(a.top() + a.radiusY() / 2) < a.radiusX(), "corner opens smoothly");
        BoxViewGeometry.Aperture b = BoxViewGeometry.calculate(320, 180);
        check(b.right() <= 320 && b.bottom() <= 180, "scaled view inside screen");
        check(b.top() < 90 && b.bottom() > 90, "crosshair remains in the opening");
        BoxViewGeometry.Aperture c = BoxViewGeometry.calculate(0, 0);
        check(c.left() == 0 && c.right() == 0, "zero dimensions handled");
        System.out.println("PASS: " + count + "/12 cardboard eye-window geometry checks");
    }
}

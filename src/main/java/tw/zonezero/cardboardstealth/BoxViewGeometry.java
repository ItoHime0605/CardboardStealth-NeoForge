package tw.zonezero.cardboardstealth;

/**
 * Pure-Java dimensions for the temporary first-person box eye opening.
 * Based on the user's 757x496 reference: a 45% x 31% rounded rectangular window
 * centred horizontally and positioned slightly above the screen's vertical centre.
 */
public final class BoxViewGeometry {
    private BoxViewGeometry() {}

    public record Aperture(int left, int top, int right, int bottom, int radiusX, int radiusY) {
        /** Number of extra pixels occluded at each side of a rounded corner row. */
        public int insetAt(int y) {
            if (y < top || y >= bottom || radiusY <= 0 || radiusX <= 0) return 0;
            int depth = Math.min(y - top, bottom - 1 - y);
            if (depth >= radiusY) return 0;
            double value = 1.0D - (double) depth / radiusY;
            return (int) Math.round(radiusX * (1.0D - Math.sqrt(Math.max(0.0D, 1.0D - value * value))));
        }
    }

    public static Aperture calculate(int width, int height) {
        if (width <= 0 || height <= 0) return new Aperture(0, 0, 0, 0, 0, 0);
        int openingWidth = Math.max(1, Math.min(width, Math.round(width * 0.45F)));
        int openingHeight = Math.max(1, Math.min(height, Math.round(height * 0.31F)));
        int left = (width - openingWidth) / 2;
        int proposedTop = Math.round(height * 0.465F) - openingHeight / 2;
        int top = Math.max(0, Math.min(height - openingHeight, proposedTop));
        int rx = Math.min(openingWidth / 2, Math.max(1, Math.round(openingWidth * 0.14F)));
        int ry = Math.min(openingHeight / 2, Math.max(1, Math.round(openingHeight * 0.18F)));
        return new Aperture(left, top, left + openingWidth, top + openingHeight, rx, ry);
    }
}

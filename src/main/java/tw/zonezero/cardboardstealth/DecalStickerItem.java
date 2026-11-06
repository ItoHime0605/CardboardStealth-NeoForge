package tw.zonezero.cardboardstealth;

import net.minecraft.world.item.Item;

/** A physical, craftable design. Pattern is fixed when this item is made. */
public final class DecalStickerItem extends Item {
    private final int color;
    private final int artwork;

    public DecalStickerItem(Properties properties, int color, int artwork) {
        super(properties);
        if (!DecalStackRules.supportedColor(color) ||
                artwork < 0 || artwork >= DecalStackRules.artCount(color))
            throw new IllegalArgumentException("Invalid physical sticker design");
        this.color = color;
        this.artwork = artwork;
    }

    public int color() { return color; }
    public int artwork() { return artwork; }
}

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/** Decode the actual JAR sprites to catch blank tints and a cropped stamp. */
public final class CardboardAssetIntegrityTest {
    private static final String[] COLORS = {
        "white","orange","magenta","light_blue","yellow","lime","pink","gray",
        "light_gray","cyan","purple","blue","brown","green","red","black"
    };
    private static int count;
    private static void check(boolean condition, String detail) {
        count++;
        if (!condition) throw new AssertionError(detail);
    }
    private static BufferedImage read(ZipFile jar, String name) throws Exception {
        var entry=jar.getEntry(name);
        if (entry==null) throw new AssertionError("Missing file: "+name);
        try(var input=jar.getInputStream(entry)){
            BufferedImage result=ImageIO.read(input);
            if(result==null)throw new AssertionError("Broken PNG: "+name);
            return result;
        }
    }
    public static void main(String[] args) throws Exception {
        try(ZipFile jar=new ZipFile(Path.of(args[0]).toFile())){
            BufferedImage[] sprites=new BufferedImage[COLORS.length];
            for(int c=0;c<COLORS.length;c++){
                String name="assets/cardboardstealth/textures/item/colored/"+COLORS[c]+"_cardboard_box.png";
                BufferedImage sprite=read(jar,name);
                check(sprite.getWidth()==64 && sprite.getHeight()==64, name+" dimension");
                int solid=0;
                for(int y=0;y<64;y++)for(int x=0;x<64;x++)
                    if((sprite.getRGB(x,y)>>>24)==255)solid++;
                check(solid>=1800, name+" must have an opaque dyed cardboard body");
                sprites[c]=sprite;
            }
            // At least one common body pixel must genuinely display all dyes.
            int widest=0;
            for(int y=0;y<64;y++)for(int x=0;x<64;x++){
                Set<Integer> shades=new HashSet<>();
                boolean allVisible=true;
                for(BufferedImage sprite:sprites){
                    int px=sprite.getRGB(x,y);
                    if((px>>>24)!=255){allVisible=false;break;}
                    shades.add(px);
                }
                if(allVisible) widest=Math.max(widest,shades.size());
            }
            check(widest>=14, "16 colored cardboard bodies must differ in actual pixel colors");
            BufferedImage logo=read(jar,"assets/cardboardstealth/textures/entity/recycle_stamp.png");
            check(logo.getWidth()==128 && logo.getHeight()==128,"logo atlas resolution");
            int stampPixels=0, spill=0;
            for(int y=0;y<128;y++)for(int x=0;x<128;x++){
                int px=logo.getRGB(x,y);
                if((px>>>24)==0)continue;
                if(x<32 && y<32){
                    stampPixels++;
                    check(px==0xFF006600,"logo remains the reference green ink");
                }else spill++;
            }
            check(stampPixels==602,"32x32 reference shape has exactly 602 opaque pixels");
            check(spill==0,"recycling print must not leak beyond its UV square");
            // Restore the actual pre-0.1.22 inventory artwork rather than the
            // newer straight-on back-view image. Verify every selected PNG.
            String[] originals={"cardboard_box","delivery_box","supply_box"};
            for(String name:originals){
                BufferedImage sprite=read(jar,
                        "assets/cardboardstealth/textures/item/"+name+".png");
                check(sprite.getWidth()==16||sprite.getWidth()==64,
                        "original "+name+" icon has the original source resolution");
                check(sprite.getWidth()==sprite.getHeight(),
                        "original "+name+" icon has square pixel-art proportions");
                int visible=0;
                for(int y=0;y<sprite.getHeight();y++)
                    for(int x=0;x<sprite.getWidth();x++)
                        if((sprite.getRGB(x,y)>>>24)>100)visible++;
                check(visible>36,"old "+name+" graphic isn't blank");
            }
            for(String color:COLORS){
                String name=color+"_cardboard_box";
                BufferedImage sprite=read(jar,
                        "assets/cardboardstealth/textures/item/colored/"+name+".png");
                check(sprite.getWidth()==64&&sprite.getHeight()==64,
                        "restore original 64x64 dyed inventory art for "+color);
            }
            for (String name : new String[]{
                    "cardboard_box","delivery_box","supply_box",
                    "white_cardboard_box","orange_cardboard_box","magenta_cardboard_box",
                    "light_blue_cardboard_box","yellow_cardboard_box","lime_cardboard_box",
                    "pink_cardboard_box","gray_cardboard_box","light_gray_cardboard_box",
                    "cyan_cardboard_box","purple_cardboard_box","blue_cardboard_box",
                    "brown_cardboard_box","green_cardboard_box","red_cardboard_box",
                    "black_cardboard_box"}) {
                BufferedImage mini=read(jar,
                        "assets/cardboardstealth/textures/item/mini/"+name+"_paper.png");
                check(mini.getWidth()==128 && mini.getHeight()==128,
                        name+" textured hand/dropped miniature is 128x128");
                check((mini.getRGB(20,20)>>>24)==255,
                        name+" miniature has fully opaque paper material");
            }
            // Item model tape/recycle material MUST be present in the item
            // atlas. Being present only in textures/entity does not suffice.
            BufferedImage miniTape=read(jar,
                    "assets/cardboardstealth/textures/item/mini/packing_tape.png");
            BufferedImage miniStamp=read(jar,
                    "assets/cardboardstealth/textures/item/mini/recycle_stamp.png");
            check(miniTape.getWidth()==128&&miniTape.getHeight()==128,
                    "stitched item miniature tape atlas is 128px");
            check(miniStamp.getWidth()==128&&miniStamp.getHeight()==128,
                    "stitched item miniature stamp atlas is 128px");
            int tan=0,opaqueTape=0,stampMatches=0;
            for(int y=0;y<128;y++)for(int x=0;x<128;x++){
                int px=miniTape.getRGB(x,y);
                int r=(px>>>16)&255,g=(px>>>8)&255,b=px&255;
                if((px>>>24)==255)opaqueTape++;
                if(r>g+15&&g>b+15)tan++;
                if(miniStamp.getRGB(x,y)==logo.getRGB(x,y))stampMatches++;
            }
            check(opaqueTape>16000,"miniature tape is a real complete opaque texture");
            check(tan>15000,"miniature tape is a beige paper seal, not magenta");
            check(stampMatches==128*128,
                    "miniature recycle glyph has the original 32x32 stamp pixels");
            System.out.println("PASS: "+count+"/"+count+" decoded dyed-item and recycle-logo pixel checks");
        }
    }
}

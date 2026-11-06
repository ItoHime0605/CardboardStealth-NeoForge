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
            // The actual selected GUI textures are now straight-on 2D sprites,
            // while the held and dropped 3D models use their own paper atlases.
            for (String name : new String[]{
                    "cardboard_box","delivery_box","supply_box",
                    "white_cardboard_box","orange_cardboard_box","magenta_cardboard_box",
                    "light_blue_cardboard_box","yellow_cardboard_box","lime_cardboard_box",
                    "pink_cardboard_box","gray_cardboard_box","light_gray_cardboard_box",
                    "cyan_cardboard_box","purple_cardboard_box","blue_cardboard_box",
                    "brown_cardboard_box","green_cardboard_box","red_cardboard_box",
                    "black_cardboard_box"}) {
                BufferedImage straight = read(jar,
                        "assets/cardboardstealth/textures/item/straight/"+name+".png");
                check(straight.getWidth()==64 && straight.getHeight()==64,
                        name+" straight-on GUI texture size");
                int solid = 0, greenInk = 0;
                for (int y=0;y<64;y++) for (int x=0;x<64;x++) {
                    int rgba=straight.getRGB(x,y);
                    if ((rgba>>>24)==255) solid++;
                    if (x>=36&&x<=48&&y>=33&&y<=45&&rgba==0xFF006600) greenInk++;
                }
                check(solid>=1800, name+" opaque full-color straight-on icon");
                check(greenInk>=4, name+" back-right recycle mark must be legible");
                BufferedImage mini=read(jar,
                        "assets/cardboardstealth/textures/item/mini/"+name+"_paper.png");
                check(mini.getWidth()==128 && mini.getHeight()==128,
                        name+" mini 3D model paper texture size");
                check((mini.getRGB(20,20)>>>24)==255,name+" mini 3D paper is not transparent");
            }
            System.out.println("PASS: "+count+"/"+count+" decoded dyed-item and recycle-logo pixel checks");
        }
    }
}

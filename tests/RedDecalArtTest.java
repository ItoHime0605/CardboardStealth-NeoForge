import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/**
 * Source-independent pixel checks: the exported JAR must contain genuine,
 * different, well-inked red pin-up posters with readable contrast.
 */
public final class RedDecalArtTest {
    private static int checked;
    private static void check(boolean condition, String message) {
        checked++;
        if (!condition) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        try (ZipFile jar=new ZipFile(Path.of(args[0]).toFile())) {
            Set<Long> fingerprints=new HashSet<>();
            for(int id=0;id<6;id++) {
                var e=jar.getEntry("assets/cardboardstealth/textures/decals/red_"+id+".png");
                check(e!=null,"missing red pin-up "+id);
                BufferedImage img;
                try(var input=jar.getInputStream(e)){ img=ImageIO.read(input); }
                check(img!=null&&img.getWidth()==128&&img.getHeight()==80,
                        "each red poster must be native 128x80");
                int opaque=0,transparent=0,deepInk=0,redInk=0,warmSkin=0;
                long hash=1469598103934665603L;
                for(int y=0;y<80;y++)for(int x=0;x<128;x++) {
                    int px=img.getRGB(x,y);
                    hash=(hash^px)*1099511628211L;
                    int alpha=px>>>24;
                    if(alpha==0)transparent++;
                    if(alpha<160)continue;
                    opaque++;
                    int red=(px>>>16)&255,green=(px>>>8)&255,blue=px&255;
                    if(red<115&&green<115&&blue<115)deepInk++;
                    if(red>105&&red>green+40&&red>blue+15)redInk++;
                    if(red>165&&green>90&&green<210&&blue<170&&red>blue+30)
                        warmSkin++;
                }
                check(opaque>3000,"fully illustrated poster not an empty placeholder "+id);
                check(transparent>120,"transparent ripped edges required "+id);
                check(deepInk>45,"poster has clear dark ink detail "+id);
                check(redInk>50,"poster contains red painted ink "+id);
                check(warmSkin>80,"poster contains a recognizable warm character portrait "+id);
                fingerprints.add(hash);
            }
            check(fingerprints.size()==6,"all six red posters must be different illustrations");
            check(jar.getEntry("assets/cardboardstealth/textures/decals/red_atlas.png")!=null,
                    "atlas is present in packaged JAR");
        }
        System.out.println("PASS: "+checked+"/"+checked+" red pin-up artwork checks");
    }
}

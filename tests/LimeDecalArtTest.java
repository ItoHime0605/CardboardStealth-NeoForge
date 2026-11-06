import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/** Inspect exported art, not only Java source or filename placeholders. */
public final class LimeDecalArtTest {
    private static int assertions;
    private static void check(boolean ok,String name) {
        assertions++;
        if(!ok)throw new AssertionError(name);
    }
    public static void main(String[] args)throws Exception{
        String root="assets/cardboardstealth/textures/decals/";
        try(ZipFile jar=new ZipFile(Path.of(args[0]).toFile())){
            Set<Long> unique=new HashSet<>();
            for(int art=0;art<6;art++){
                var entry=jar.getEntry(root+"lime_"+art+".png");
                check(entry!=null,"lime botany art "+art+" is packaged");
                BufferedImage img;
                try(var in=jar.getInputStream(entry)){img=ImageIO.read(in);}
                check(img!=null && img.getWidth()==128 && img.getHeight()==80,
                    "each new botanical print is full 128x80 resolution");
                int visible=0,clear=0,green=0,dark=0,highlight=0;
                long fingerprint=1469598103934665603L;
                for(int y=0;y<80;y++)for(int x=0;x<128;x++){
                    int p=img.getRGB(x,y);
                    fingerprint=(fingerprint^p)*1099511628211L;
                    int a=p>>>24;
                    if(a==0)clear++;
                    if(a<170)continue;
                    visible++;
                    int r=(p>>>16)&255,g=(p>>>8)&255,b=p&255;
                    if(g>r+12 && g>b+6 && g>95)green++;
                    if(r<105 && g<125 && b<115)dark++;
                    if(r>160 && g>185 && b>110)highlight++;
                }
                check(visible>3000,"botanical art "+art+" contains actual illustration");
                check(clear>120,"botanical art "+art+" has torn transparent edges");
                check(green>120,"botanical art "+art+" has vivid foliage pigments");
                check(dark>100,"botanical art "+art+" has dark ink and readable framing");
                check(highlight>40,"botanical art "+art+" has light print contrast");
                unique.add(fingerprint);
            }
            check(unique.size()==6,"all six lime prints are different compositions");
            var atlas=jar.getEntry(root+"lime_atlas.png");
            check(atlas!=null,"botanical sticker atlas must be packaged");
            BufferedImage img;
            try(var in=jar.getInputStream(atlas)){img=ImageIO.read(in);}
            check(img!=null && img.getWidth()==256 && img.getHeight()==240,
                    "botanical atlas uses 2x3 native sprites");
        }
        System.out.println("PASS: "+assertions+"/"+assertions+" lime botanical sticker pixel tests");
    }
}

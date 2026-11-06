import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/** Verify cyan weather-station art in the actual packaged JAR, not filenames. */
public final class CyanDecalArtTest {
    private static int assertions;
    private static void check(boolean ok,String explanation) {
        assertions++;
        if(!ok)throw new AssertionError(explanation);
    }
    public static void main(String[] args)throws Exception {
        String base="assets/cardboardstealth/textures/decals/";
        try(ZipFile jar=new ZipFile(Path.of(args[0]).toFile())) {
            Set<Long> unique=new HashSet<>();
            for(int id=0;id<6;id++) {
                var entry=jar.getEntry(base+"cyan_"+id+".png");
                check(entry!=null,"missing finished cyan weather illustration "+id);
                BufferedImage img;
                try(var stream=jar.getInputStream(entry)){img=ImageIO.read(stream);}
                check(img!=null && img.getWidth()==128 && img.getHeight()==80,
                      "cyan diagram must be 128x80 pixels "+id);
                int ink=0,clear=0,cyan=0,dark=0,pale=0;
                long hash=1469598103934665603L;
                for(int y=0;y<80;y++)for(int x=0;x<128;x++){
                    int pixel=img.getRGB(x,y);
                    hash=(hash^pixel)*1099511628211L;
                    int a=pixel>>>24;
                    if(a==0)clear++;
                    if(a<170)continue;
                    ink++;
                    int r=(pixel>>>16)&255,g=(pixel>>>8)&255,b=pixel&255;
                    if(g>r+20 && b>r+17 && g>125 && b>130)cyan++;
                    if(r<120 && g<135 && b<150)dark++;
                    if(r>150 && g>175 && b>160)pale++;
                }
                check(ink>3000,"cyan print contains a full diagram "+id);
                check(clear>120,"cyan print has true transparent torn corners "+id);
                check(cyan>140,"cyan print uses substantial blue-green pigments "+id);
                check(dark>100,"cyan print includes readable dark radar/grid ink "+id);
                check(pale>140,"cyan print preserves pale field-chart contrast "+id);
                unique.add(hash);
            }
            check(unique.size()==6,"six unique cyan weather diagrams, not recolors");
            var atlas=jar.getEntry(base+"cyan_atlas.png");
            check(atlas!=null,"cyan atlas exists in packaged mod");
            BufferedImage image;
            try(var stream=jar.getInputStream(atlas)){image=ImageIO.read(stream);}
            check(image!=null && image.getWidth()==256 && image.getHeight()==240,
                  "cyan atlas uses 2 by 3 native 128x80 cells");
        }
        System.out.println("PASS: "+assertions+"/"+assertions+
                " original cyan weather observatory artwork checks");
    }
}

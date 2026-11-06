import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/** Inspect finished JAR sprites rather than merely checking generator source. */
public final class PurpleDecalArtTest {
    private static int assertions;
    private static void check(boolean success,String description) {
        assertions++;
        if(!success)throw new AssertionError(description);
    }
    public static void main(String[] args)throws Exception {
        String root="assets/cardboardstealth/textures/decals/";
        try(ZipFile jar=new ZipFile(Path.of(args[0]).toFile())) {
            Set<Long> fingerprints=new HashSet<>();
            for(int id=0;id<6;id++){
                var entry=jar.getEntry(root+"purple_"+id+".png");
                check(entry!=null,"original purple metro sticker "+id+" included");
                BufferedImage img;
                try(var input=jar.getInputStream(entry)){img=ImageIO.read(input);}
                check(img!=null&&img.getWidth()==128&&img.getHeight()==80,
                        "purple metro sticker "+id+" uses 128x80 source pixels");
                int visible=0,clear=0,purple=0,dark=0,pale=0;
                long fingerprint=1469598103934665603L;
                for(int y=0;y<80;y++)for(int x=0;x<128;x++){
                    int px=img.getRGB(x,y);
                    fingerprint=(fingerprint^px)*1099511628211L;
                    int alpha=px>>>24;
                    if(alpha==0)clear++;
                    if(alpha<170)continue;
                    visible++;
                    int r=(px>>>16)&255,g=(px>>>8)&255,b=px&255;
                    if(b>g+14&&r>g+8&&b>90)purple++;
                    if(r<125&&g<120&&b<155)dark++;
                    if(r>165&&g>145&&b>155)pale++;
                }
                check(visible>3000,"purple metro poster "+id+" is illustrated");
                check(clear>120,"purple metro poster "+id+" has ripped transparent edges");
                check(purple>140,"purple metro poster "+id+" has violet inks");
                check(dark>100,"purple metro poster "+id+" has dark contrast");
                check(pale>130,"purple metro poster "+id+" has vintage light ink");
                fingerprints.add(fingerprint);
            }
            check(fingerprints.size()==6,"six truly different purple metro poster compositions");
            var atlas=jar.getEntry(root+"purple_atlas.png");
            check(atlas!=null,"purple metro atlas packaged");
            BufferedImage output;
            try(var input=jar.getInputStream(atlas)){output=ImageIO.read(input);}
            check(output!=null&&output.getWidth()==256&&output.getHeight()==240,
                    "purple atlas dimensions match 2-by-3 original source sprites");
        }
        System.out.println("PASS: "+assertions+"/"+assertions+" purple metro artwork checks");
    }
}

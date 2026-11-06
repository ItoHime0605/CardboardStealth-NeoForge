import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/** Validate finished medical posters in JAR, not just source placeholders. */
public final class MagentaDecalArtTest {
    private static int assertions;
    private static void check(boolean valid,String explanation){
        assertions++;
        if(!valid)throw new AssertionError(explanation);
    }
    public static void main(String[] args)throws Exception{
        String root="assets/cardboardstealth/textures/decals/";
        try(ZipFile jar=new ZipFile(Path.of(args[0]).toFile())){
            Set<Long> fingerprints=new HashSet<>();
            for(int id=0;id<6;id++){
                var entry=jar.getEntry(root+"magenta_"+id+".png");
                check(entry!=null,"magenta medical decal "+id+" is present");
                BufferedImage img;
                try(var in=jar.getInputStream(entry)){img=ImageIO.read(in);}
                check(img!=null&&img.getWidth()==128&&img.getHeight()==80,
                        "magenta medical source "+id+" is native 128x80");
                int visible=0,transparent=0,rose=0,dark=0,pale=0;
                long fingerprint=1469598103934665603L;
                for(int y=0;y<80;y++)for(int x=0;x<128;x++){
                    int px=img.getRGB(x,y);
                    fingerprint=(fingerprint^px)*1099511628211L;
                    int a=px>>>24;
                    if(a==0)transparent++;
                    if(a<170)continue;
                    visible++;
                    int r=(px>>>16)&255,g=(px>>>8)&255,b=px&255;
                    if(r>g+45&&b>g+20&&r>120&&b>90)rose++;
                    if(r<120&&g<125&&b<140)dark++;
                    if(r>160&&g>140&&b>145)pale++;
                }
                check(visible>3000,"magenta poster "+id+" has substantial illustration");
                check(transparent>120,"magenta poster "+id+" has torn transparent edges");
                check(rose>110,"magenta poster "+id+" has distinctive rose-magenta ink");
                check(dark>70,"magenta poster "+id+" has dark legible detail");
                check(pale>120,"magenta poster "+id+" has light print/paper contrast");
                fingerprints.add(fingerprint);
            }
            check(fingerprints.size()==6,"six independent medical pictograms, not duplicated art");
            var atlas=jar.getEntry(root+"magenta_atlas.png");
            check(atlas!=null,"magenta 2x3 atlas included in packed NeoForge JAR");
            BufferedImage image;
            try(var in=jar.getInputStream(atlas)){image=ImageIO.read(in);}
            check(image!=null&&image.getWidth()==256&&image.getHeight()==240,
                    "magenta atlas must be 256x240 for six 128x80 original prints");
        }
        System.out.println("PASS: "+assertions+"/"+assertions+" magenta field-aid art checks");
    }
}

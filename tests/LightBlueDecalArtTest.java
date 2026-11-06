import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/** Check final 16th family's actual packaged pictures, not placeholder names. */
public final class LightBlueDecalArtTest {
    private static int checks;
    private static void check(boolean ok,String reason){
        checks++;
        if(!ok)throw new AssertionError(reason);
    }
    public static void main(String[] args)throws Exception {
        String root="assets/cardboardstealth/textures/decals/";
        try(ZipFile jar=new ZipFile(Path.of(args[0]).toFile())){
            Set<Long> fingerprints=new HashSet<>();
            for(int id=0;id<6;id++){
                var entry=jar.getEntry(root+"light_blue_"+id+".png");
                check(entry!=null,"missing 16th-family original airfield drawing "+id);
                BufferedImage img;
                try(var in=jar.getInputStream(entry)){img=ImageIO.read(in);}
                check(img!=null&&img.getWidth()==128&&img.getHeight()==80,
                        "light-blue airfield print "+id+" is full 128x80 pixels");
                int painted=0,clear=0,sky=0,dark=0,pale=0;
                long hash=1469598103934665603L;
                for(int y=0;y<80;y++)for(int x=0;x<128;x++){
                    int px=img.getRGB(x,y);
                    hash=(hash^px)*1099511628211L;
                    int a=px>>>24;
                    if(a==0)clear++;
                    if(a<170)continue;
                    painted++;
                    int r=(px>>>16)&255,g=(px>>>8)&255,b=px&255;
                    if(b>r+13&&b>g+9&&g>95&&b>118)sky++;
                    if(r<125&&g<135&&b<155)dark++;
                    if(r>160&&g>165&&b>150)pale++;
                }
                check(painted>3000,"airfield print "+id+" is an illustrated poster");
                check(clear>120,"airfield print "+id+" has ragged transparent paper edges");
                check(sky>120,"airfield print "+id+" has blue aviation-chart ink");
                check(dark>100,"airfield print "+id+" has dark readable print details");
                check(pale>150,"airfield print "+id+" has pale faded-paper contrast");
                fingerprints.add(hash);
            }
            check(fingerprints.size()==6,"six independently drawn light-blue airfield subjects");
            var entry=jar.getEntry(root+"light_blue_atlas.png");
            check(entry!=null,"six-source light-blue atlas included");
            BufferedImage atlas;
            try(var in=jar.getInputStream(entry)){atlas=ImageIO.read(in);}
            check(atlas!=null&&atlas.getWidth()==256&&atlas.getHeight()==240,
                    "last-family 2x3 source atlas resolution is 256x240");
        }
        System.out.println("PASS: "+checks+"/"+checks+
                " original light-blue lost-airfield pixel-art checks");
    }
}

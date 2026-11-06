import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/** Checks actual decoded pixels from packaged orange roadside poster art. */
public final class OrangeDecalArtTest {
    private static int checks;
    private static void check(boolean ok,String explanation){
        checks++;
        if(!ok)throw new AssertionError(explanation);
    }
    public static void main(String[] args)throws Exception{
        try(ZipFile zip=new ZipFile(Path.of(args[0]).toFile())){
            Set<Long> distinct=new HashSet<>();
            String root="assets/cardboardstealth/textures/decals/";
            for(int id=0;id<6;id++){
                var entry=zip.getEntry(root+"orange_"+id+".png");
                check(entry!=null,"missing orange art "+id);
                BufferedImage img;
                try(var stream=zip.getInputStream(entry)){img=ImageIO.read(stream);}
                check(img!=null&&img.getWidth()==128&&img.getHeight()==80,
                      "orange poster must be 128x80, index "+id);
                int ink=0,transparent=0,dark=0,warm=0;
                long hash=1469598103934665603L;
                for(int y=0;y<80;y++)for(int x=0;x<128;x++){
                    int px=img.getRGB(x,y);
                    hash=(hash^px)*1099511628211L;
                    int alpha=px>>>24;
                    if(alpha==0)transparent++;
                    if(alpha<160)continue;
                    ink++;
                    int r=(px>>>16)&255,g=(px>>>8)&255,b=px&255;
                    if(r<110&&g<115&&b<125)dark++;
                    if(r>=140&&r>g+25&&g>b+10)warm++;
                }
                check(ink>3000,"not a full hand-illustrated orange poster "+id);
                check(transparent>120,"orange art needs transparent torn borders "+id);
                check(dark>50,"details and strong printing contrast "+id);
                check(warm>50,"warm roadside orange/rust/gold printing "+id);
                distinct.add(hash);
            }
            check(distinct.size()==6,"six genuinely different orange roadside drawings");
            check(zip.getEntry(root+"orange_atlas.png")!=null,
                    "packaged 256x240 atlas must exist");
        }
        System.out.println("PASS: "+checks+"/"+checks+" orange roadside poster art checks");
    }
}

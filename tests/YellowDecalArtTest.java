import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/** Verify six actual yellow illustrated hazard decals in built mod JAR. */
public final class YellowDecalArtTest {
    private static int count;
    private static void test(boolean ok,String message){
        count++;
        if(!ok)throw new AssertionError(message);
    }
    public static void main(String[] args)throws Exception {
        String base="assets/cardboardstealth/textures/decals/";
        try(ZipFile z=new ZipFile(Path.of(args[0]).toFile())){
            Set<Long> signatures=new HashSet<>();
            for(int i=0;i<6;i++){
                var entry=z.getEntry(base+"yellow_"+i+".png");
                test(entry!=null,"missing yellow hazard art "+i);
                BufferedImage img;
                try(var in=z.getInputStream(entry)){img=ImageIO.read(in);}
                test(img!=null && img.getWidth()==128 && img.getHeight()==80,
                    "128x80 original hazard graphic "+i);
                int painted=0,transparent=0,yellow=0,black=0;
                long hash=1469598103934665603L;
                for(int y=0;y<80;y++)for(int x=0;x<128;x++){
                    int pixel=img.getRGB(x,y);
                    hash=(hash^pixel)*1099511628211L;
                    int a=pixel>>>24;
                    if(a==0)transparent++;
                    if(a<170)continue;
                    painted++;
                    int r=(pixel>>>16)&255,g=(pixel>>>8)&255,b=pixel&255;
                    if(r>160 && g>125 && b<190 && r>b+45)yellow++;
                    if(r<120 && g<120 && b<120)black++;
                }
                test(painted>3000,"yellow hazard is not a full pictogram "+i);
                test(transparent>120,"transparent torn edges "+i);
                test(yellow>300,"bright safety-yellow/yellowed ink "+i);
                test(black>80,"readable charcoal symbols and borders "+i);
                signatures.add(hash);
            }
            test(signatures.size()==6,"all six hazards are distinct illustrations");
            test(z.getEntry(base+"yellow_atlas.png")!=null,"final seven-family atlas exists");
        }
        System.out.println("PASS: "+count+"/"+count+" yellow illustrated hazard art checks");
    }
}

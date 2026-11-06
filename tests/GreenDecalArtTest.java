import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/** Check decoded, packaged green outpost decals rather than source filenames. */
public final class GreenDecalArtTest {
    private static int checks;
    private static void test(boolean condition,String reason){
        checks++;
        if(!condition)throw new AssertionError(reason);
    }
    public static void main(String[] args)throws Exception{
        String dir="assets/cardboardstealth/textures/decals/";
        try(ZipFile jar=new ZipFile(Path.of(args[0]).toFile())){
            Set<Long> signatures=new HashSet<>();
            for(int id=0;id<6;id++){
                var entry=jar.getEntry(dir+"green_"+id+".png");
                test(entry!=null,"missing packaged green outpost poster "+id);
                BufferedImage img;
                try(var stream=jar.getInputStream(entry)){img=ImageIO.read(stream);}
                test(img!=null&&img.getWidth()==128&&img.getHeight()==80,
                        "poster "+id+" must be full-resolution 128x80");
                int visible=0,clear=0,olive=0,dark=0,pale=0;
                long hash=1469598103934665603L;
                for(int y=0;y<80;y++)for(int x=0;x<128;x++){
                    int pixel=img.getRGB(x,y);
                    hash=(hash^pixel)*1099511628211L;
                    int a=pixel>>>24;
                    if(a==0)clear++;
                    if(a<170)continue;
                    visible++;
                    int r=(pixel>>>16)&255,g=(pixel>>>8)&255,b=pixel&255;
                    if(g>r+5&&g>b+7&&g>=70)olive++;
                    if(r<115&&g<125&&b<115)dark++;
                    if(r>165&&g>165&&b>105)pale++;
                }
                test(visible>3000,"green poster "+id+" has actual painted detail");
                test(clear>120,"green poster "+id+" has transparent ragged edges");
                test(olive>200,"green poster "+id+" has olive field inks");
                test(dark>80,"green poster "+id+" has dark stencil contrast");
                test(pale>150,"green poster "+id+" has pale canvas highlights");
                signatures.add(hash);
            }
            test(signatures.size()==6,"six individual green outpost designs exist");
            var atlas=jar.getEntry(dir+"green_atlas.png");
            test(atlas!=null,"green decal atlas in packaged JAR");
            BufferedImage img;
            try(var stream=jar.getInputStream(atlas)){img=ImageIO.read(stream);}
            test(img!=null&&img.getWidth()==256&&img.getHeight()==240,
                    "green atlas is 256x240 pixels");
        }
        System.out.println("PASS: "+checks+"/"+checks+" green outpost poster pixel tests");
    }
}

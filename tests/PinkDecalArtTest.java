import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/** Inspect six actual packaged drawings, not placeholder paths or source names. */
public final class PinkDecalArtTest {
    private static int count;
    private static void check(boolean valid,String description) {
        count++;
        if(!valid)throw new AssertionError(description);
    }
    public static void main(String[] args) throws Exception {
        String root="assets/cardboardstealth/textures/decals/";
        try(ZipFile jar=new ZipFile(Path.of(args[0]).toFile())){
            Set<Long> signatures=new HashSet<>();
            for(int id=0;id<6;id++){
                var entry=jar.getEntry(root+"pink_"+id+".png");
                check(entry!=null,"missing packaged original pink poster "+id);
                BufferedImage image;
                try(var input=jar.getInputStream(entry)){image=ImageIO.read(input);}
                check(image!=null&&image.getWidth()==128&&image.getHeight()==80,
                      "pink sticker "+id+" must be native 128 by 80");
                int printed=0,clear=0,rose=0,dark=0,light=0;
                long fingerprint=1469598103934665603L;
                for(int y=0;y<80;y++)for(int x=0;x<128;x++){
                    int pixel=image.getRGB(x,y);
                    fingerprint=(fingerprint^pixel)*1099511628211L;
                    int alpha=pixel>>>24;
                    if(alpha==0)clear++;
                    if(alpha<170)continue;
                    printed++;
                    int red=(pixel>>>16)&255,green=(pixel>>>8)&255,blue=pixel&255;
                    if(red>135&&red>green+35&&blue>65&&blue<180)rose++;
                    if(red<115&&green<115&&blue<135)dark++;
                    if(red>185&&green>175&&blue>130)light++;
                }
                check(printed>3000,"pink drawing "+id+" is not a blank image");
                check(clear>120,"pink drawing "+id+" must retain ragged transparent edges");
                check(rose>160,"pink drawing "+id+" must contain vivid rose print");
                check(dark>65,"pink drawing "+id+" needs readable dark ink contrast");
                check(light>65,"pink drawing "+id+" needs light screen-print contrast");
                signatures.add(fingerprint);
            }
            check(signatures.size()==6,
                  "no duplicate graphics: six independent pink street-culture artworks");
            var atlas=jar.getEntry(root+"pink_atlas.png");
            check(atlas!=null,"pink 2x3 atlas packaged alongside original source sprites");
            BufferedImage raster;
            try(var in=jar.getInputStream(atlas)){raster=ImageIO.read(in);}
            check(raster!=null&&raster.getWidth()==256&&raster.getHeight()==240,
                  "pink atlas has exactly 256 by 240 pixels");
        }
        System.out.println("PASS: "+count+"/"+count+" pink wasteland street-culture artwork checks");
    }
}

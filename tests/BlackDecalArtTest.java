import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/** Additional visual regression checks on decoded output, not placeholder filenames. */
public final class BlackDecalArtTest {
    private static int checked=0;
    private static void require(boolean condition,String message){
        checked++;
        if(!condition)throw new AssertionError(message);
    }
    public static void main(String[] args)throws Exception{
        try(ZipFile jar=new ZipFile(Path.of(args[0]).toFile())){
            Set<Long> unique=new HashSet<>();
            for(int id=0;id<6;id++){
                var entry=jar.getEntry("assets/cardboardstealth/textures/decals/black_"+id+".png");
                require(entry!=null,"Black carpet sticker "+id+" is included");
                BufferedImage img;
                try(var in=jar.getInputStream(entry)){img=ImageIO.read(in);}
                require(img!=null&&img.getWidth()==128&&img.getHeight()==80,
                        "Full-resolution 128x80 source-art sprite "+id);
                int visible=0,clear=0,dark=0,highlight=0,accent=0;
                long hash=1125899906842597L;
                for(int y=0;y<80;y++)for(int x=0;x<128;x++){
                    int pixel=img.getRGB(x,y);
                    hash=31L*hash+pixel;
                    int alpha=pixel>>>24;
                    if(alpha==0)clear++;
                    if(alpha>=170){
                        visible++;
                        int rr=(pixel>>>16)&255,gg=(pixel>>>8)&255,bb=pixel&255;
                        int avg=(rr+gg+bb)/3;
                        if(avg<90)dark++;
                        if(avg>175)highlight++;
                        if((rr>gg+32&&rr>bb+12)||(gg>rr+19&&bb>rr+20))accent++;
                    }
                }
                require(visible>3200,"Black sticker "+id+" is detailed, not empty");
                require(clear>180,"Black sticker "+id+" has torn/transparent edges");
                require(dark>250,"Real charcoal stencil body "+id);
                require(highlight>75,"Readable contrast in black spray "+id);
                require(accent>15,"Colored paint/grunge accents "+id);
                unique.add(hash);
            }
            require(unique.size()==6,"Six distinct original black carpet artworks");
            System.out.println("PASS: "+checked+"/"+checked+" black decal contrast/transparency/art identity checks");
        }
    }
}

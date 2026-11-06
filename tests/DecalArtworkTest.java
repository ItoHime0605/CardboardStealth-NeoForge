import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

public final class DecalArtworkTest {
    private static int tests;
    private static void ok(boolean condition,String name){tests++;if(!condition)throw new AssertionError(name);}
    public static void main(String[] args) throws Exception {
        try(ZipFile z=new ZipFile(Path.of(args[0]).toFile())){
            String root="assets/cardboardstealth/textures/decals/";
            var atlasEntry=z.getEntry(root+"white_atlas.png");
            ok(atlasEntry!=null,"white decal atlas included");
            BufferedImage atlas;
            try(var in=z.getInputStream(atlasEntry)){atlas=ImageIO.read(in);}
            ok(atlas.getWidth()==256 && atlas.getHeight()==240,"two by three decal atlas");
            Set<Long> signature=new HashSet<>();
            for(int i=0;i<6;i++){
                String path=root+"white_"+i+".png";
                var entry=z.getEntry(path);
                ok(entry!=null,"art "+i+" exists");
                BufferedImage img;
                try(var in=z.getInputStream(entry)){img=ImageIO.read(in);}
                ok(img.getWidth()==128 && img.getHeight()==80,"art "+i+" resolution");
                int ink=0, transparent=0;
                long sum=0;
                for(int y=0;y<80;y++) for(int x=0;x<128;x++){
                    int pixel=img.getRGB(x,y);
                    sum=sum*31+pixel*7L+x+y;
                    if((pixel>>>24)>192)ink++;
                    if((pixel>>>24)==0)transparent++;
                    ok(atlas.getRGB(x+(i%2)*128,y+(i/2)*80)==pixel,
                            "atlas and individual art must match");
                }
                ok(ink>3000,"sticker contains detailed ink and paper");
                ok(transparent>120,"true transparent torn edge");
                signature.add(sum);
            }
            ok(signature.size()==6,"all six white carpet prints are different");
            System.out.println("PASS: "+tests+"/"+tests+" generated individual/atlas art pixel checks");
        }
    }
}

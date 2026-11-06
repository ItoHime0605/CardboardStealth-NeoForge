import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/** Assert actual finished blue maritime decals from the NeoForge JAR. */
public final class BlueDecalArtTest {
    private static int tests;
    private static void require(boolean success,String reason){
        tests++;
        if(!success)throw new AssertionError(reason);
    }
    public static void main(String[] args)throws Exception{
        String root="assets/cardboardstealth/textures/decals/";
        try(ZipFile jar=new ZipFile(Path.of(args[0]).toFile())){
            Set<Long> fingerprints=new HashSet<>();
            for(int id=0;id<6;id++){
                var entry=jar.getEntry(root+"blue_"+id+".png");
                require(entry!=null,"original blue harbor sticker "+id+" packaged");
                BufferedImage image;
                try(var input=jar.getInputStream(entry)){image=ImageIO.read(input);}
                require(image!=null&&image.getWidth()==128&&image.getHeight()==80,
                        "blue harbor illustration "+id+" uses 128x80 native pixels");
                int visible=0,transparent=0,navy=0,blue=0,light=0;
                long fingerprint=1469598103934665603L;
                for(int y=0;y<80;y++)for(int x=0;x<128;x++){
                    int px=image.getRGB(x,y);
                    fingerprint=(fingerprint^px)*1099511628211L;
                    int alpha=px>>>24;
                    if(alpha==0)transparent++;
                    if(alpha<170)continue;
                    visible++;
                    int r=(px>>>16)&255,g=(px>>>8)&255,b=px&255;
                    if(b>r+20 && b>g+9 && b>85)blue++;
                    if(r<110 && g<125 && b<145)navy++;
                    if(r>160 && g>160 && b>145)light++;
                }
                require(visible>3000,"blue poster "+id+" must contain substantial painted art");
                require(transparent>120,"blue poster "+id+" has transparent worn edges");
                require(blue>140,"blue poster "+id+" contains actual blue sea-chart inks");
                require(navy>80,"blue poster "+id+" has deep-blue engraved detail");
                require(light>150,"blue poster "+id+" retains light aged-paper contrast");
                fingerprints.add(fingerprint);
            }
            require(fingerprints.size()==6,
                    "all six blue naval posters have unique pixel compositions");
            var atlas=jar.getEntry(root+"blue_atlas.png");
            require(atlas!=null,"packaged blue 2-column/3-row atlas exists");
            BufferedImage image;
            try(var input=jar.getInputStream(atlas)){image=ImageIO.read(input);}
            require(image!=null&&image.getWidth()==256&&image.getHeight()==240,
                    "blue harbor atlas has correct 256x240 resolution");
        }
        System.out.println("PASS: "+tests+"/"+tests+" original blue harbor decal pixel tests");
    }
}

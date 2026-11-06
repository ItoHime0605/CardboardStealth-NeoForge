import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.zip.ZipFile;
import javax.imageio.ImageIO;

/** Validate actual brown woodcut-style survival prints inside the built JAR. */
public final class BrownDecalArtTest {
    private static int assertions;
    private static void require(boolean ok, String message) {
        assertions++;
        if (!ok) throw new AssertionError(message);
    }
    public static void main(String[] args) throws Exception {
        String root="assets/cardboardstealth/textures/decals/";
        try (ZipFile zip=new ZipFile(Path.of(args[0]).toFile())) {
            Set<Long> fingerprints=new HashSet<>();
            for (int id=0; id<6; id++) {
                var entry=zip.getEntry(root+"brown_"+id+".png");
                require(entry!=null,"brown survival poster "+id+" packaged");
                BufferedImage img;
                try (var in=zip.getInputStream(entry)) { img=ImageIO.read(in); }
                require(img!=null && img.getWidth()==128 && img.getHeight()==80,
                    "survival poster "+id+" should have native 128x80 art");
                int painted=0,transparent=0,brown=0,dark=0,light=0;
                long hash=1469598103934665603L;
                for (int y=0; y<80; y++) for (int x=0; x<128; x++) {
                    int px=img.getRGB(x,y);
                    hash=(hash^px)*1099511628211L;
                    int alpha=px>>>24;
                    if (alpha==0) transparent++;
                    if (alpha<170) continue;
                    painted++;
                    int r=(px>>>16)&255,g=(px>>>8)&255,b=px&255;
                    if (r>=95 && r>g+20 && g>b+4 && b<155) brown++;
                    if (r<125 && g<115 && b<110) dark++;
                    if (r>180 && g>140 && b>100) light++;
                }
                require(painted>3000,"brown poster "+id+" is not a blank image");
                require(transparent>120,"brown poster "+id+" has torn edges");
                require(brown>140,"brown poster "+id+" uses sepia woodcut ink");
                require(dark>80,"brown poster "+id+" includes dark printed structure");
                require(light>100,"brown poster "+id+" includes aged paper highlights");
                fingerprints.add(hash);
            }
            require(fingerprints.size()==6,
                    "all six brown survival drawings have unique compositions");
            var atlas=zip.getEntry(root+"brown_atlas.png");
            require(atlas!=null,"brown atlas is included");
            BufferedImage sheet;
            try (var in=zip.getInputStream(atlas)) { sheet=ImageIO.read(in); }
            require(sheet!=null && sheet.getWidth()==256 && sheet.getHeight()==240,
                    "brown atlas uses 256x240 2-by-3 layout");
        }
        System.out.println("PASS: "+assertions+"/"+assertions+
                " original brown survival illustration pixel checks");
    }
}

import tw.zonezero.cardboardstealth.DecalStackRules;
import java.util.Arrays;
public final class DecalStackRulesTest {
    private static int count=0;
    private static void check(boolean ok,String name){count++;if(!ok)throw new AssertionError(name);}
    public static void main(String[] args) {
        int[] base = {};
        for(int i=0;i<3;i++){
            int c=DecalStackRules.pack(0,i,i);
            check(DecalStackRules.isValid(c),"valid white #"+i);
            check(DecalStackRules.color(c)==0,"color");
            check(DecalStackRules.art(c)==i,"art");
            check(DecalStackRules.anchor(c)==i,"anchor");
            base=DecalStackRules.append(base,c);
            check(base.length==i+1,"appended layer");
        }
        check(DecalStackRules.append(base,DecalStackRules.pack(0,5,0)).length==3,
                "fourth application forbidden");
        check(DecalStackRules.peel(base).length==2,"remove last one only");
        check(Arrays.equals(DecalStackRules.peel(base),Arrays.copyOf(base,2)),
                "underlying layers preserved in original order");
        check(DecalStackRules.peel(new int[0]).length==0,"empty peel");
        check(DecalStackRules.sanitized(new int[]{-10,base[0],0,base[1],base[2],base[0]}).length==3,
                "reject invalid data and enforce three layers");
        for(int v=1;v<=19;v++)for(int slot=0;slot<9;slot++)
            check(DecalStackRules.nextWhiteArt(v,0,slot)>=0 &&
                    DecalStackRules.nextWhiteArt(v,0,slot)<6,"white palette variant");
        // Adding mixed carpet colors must not reorder old artwork or the
        // later-on-top semantics when the original stack is decoded again.
        int white=DecalStackRules.pack(DecalStackRules.WHITE,2,4);
        int gray=DecalStackRules.pack(DecalStackRules.LIGHT_GRAY,5,4);
        int white2=DecalStackRules.pack(DecalStackRules.WHITE,0,4);
        int[] combined=DecalStackRules.append(
                DecalStackRules.append(DecalStackRules.append(new int[0],white),gray),white2);
        check(combined.length==3,"mixed decals fill exactly three slots");
        check(Arrays.equals(combined,new int[]{white,gray,white2}),"mixed decal order preserved");
        check(DecalStackRules.color(combined[1])==DecalStackRules.LIGHT_GRAY,
                "middle layer remains light-gray");
        check(Arrays.equals(DecalStackRules.peel(combined),new int[]{white,gray}),
                "slime peels only uppermost mixed-color decal");
        check(Arrays.equals(DecalStackRules.peel(DecalStackRules.peel(combined)),
                new int[]{white}),"second peel leaves original white layer");
        check(DecalStackRules.append(combined,gray).length==3,
                "fourth mixed-color sticker is rejected");
        for(int col=0;col<=5;col++)for(int art=0;art<6;art++)for(int anchor=0;anchor<9;anchor++) {
            int entry=DecalStackRules.pack(col,art,anchor);
            check(DecalStackRules.isValid(entry),"all real art codes are valid");
            check(DecalStackRules.color(entry)==col,"carpet color survives packing");
            check(DecalStackRules.art(entry)==art,"art number survives packing");
            check(DecalStackRules.anchor(entry)==anchor,"location survives packing");
        }
        for(int col=6;col<16;col++){
            check(!DecalStackRules.supportedColor(col),"unfinished carpet not implemented yet");
            check(!DecalStackRules.isValid(DecalStackRules.pack(col,0,0)),
                    "unsupported art rejected, no blank texture");
        }
        for(int v=1;v<=19;v++)for(int slot=0;slot<9;slot++){
            int art=DecalStackRules.nextArt(DecalStackRules.LIGHT_GRAY,v,1,slot);
            check(art>=0 && art<6,"light gray art is from the six finished stickers");
        }
        int grayNew=DecalStackRules.pack(DecalStackRules.GRAY,4,4);
        int lightGray=DecalStackRules.pack(DecalStackRules.LIGHT_GRAY,3,4);
        int whiteLast=DecalStackRules.pack(DecalStackRules.WHITE,0,4);
        int[] tricolor=DecalStackRules.append(
                DecalStackRules.append(DecalStackRules.append(new int[0],grayNew),lightGray),whiteLast);
        check(Arrays.equals(tricolor,new int[]{grayNew,lightGray,whiteLast}),
                "three carpet families preserve oldest-to-newest order on shared face");
        check(Arrays.equals(DecalStackRules.peel(tricolor),new int[]{grayNew,lightGray}),
                "slime removes the last white decal from tricolor box");
        check(Arrays.equals(DecalStackRules.peel(DecalStackRules.peel(tricolor)),new int[]{grayNew}),
                "two peels leave original gray paint intact");
        check(DecalStackRules.append(tricolor,grayNew).length==3,"cannot exceed three tricolor layers");
        for(int v=1;v<=19;v++) for(int slot=0;slot<9;slot++) {
            int art=DecalStackRules.nextArt(DecalStackRules.GRAY,v,2,slot);
            check(art>=0 && art<6,"gray carpet resolves a supported high quality decal");
        }
        check(DecalStackRules.supportedColor(DecalStackRules.GRAY),"gray family is now active");
        check(DecalStackRules.artCount(DecalStackRules.GRAY)==6,"exactly 6 gray artworks");
        check(!DecalStackRules.isValid(DecalStackRules.pack(2,6,0)),
                "gray art ID 6 rejected rather than rendering nothing");
        // New black art must coexist with every already-saved three-layer
        // combination; a black sticker is a regular newest-on-top layer.
        int black=DecalStackRules.pack(DecalStackRules.BLACK,5,4);
        int[] mixed=DecalStackRules.append(
                DecalStackRules.append(DecalStackRules.append(new int[0],
                        DecalStackRules.pack(DecalStackRules.WHITE,0,4)),
                        DecalStackRules.pack(DecalStackRules.GRAY,2,4)), black);
        check(mixed.length==3, "three mixed families with black on top");
        check(mixed[2]==black, "black decal appended after gray");
        check(DecalStackRules.peel(mixed)[1]==DecalStackRules.pack(DecalStackRules.GRAY,2,4),
                "peeling black leaves the original gray");
        check(DecalStackRules.append(mixed,black).length==3, "no fourth black layer");
        check(DecalStackRules.isValid(black), "black artwork decodes correctly");
        check(DecalStackRules.color(black)==DecalStackRules.BLACK, "black color tag preserved");
        check(DecalStackRules.artCount(DecalStackRules.BLACK)==6, "six completed black designs");
        check(!DecalStackRules.isValid(DecalStackRules.pack(DecalStackRules.BLACK,6,4)),
                "missing black art never stored as an invisible layer");
        for(int variant=1;variant<=19;variant++)for(int slot=0;slot<9;slot++){
            int art=DecalStackRules.nextArt(DecalStackRules.BLACK,variant,2,slot);
            check(art>=0&&art<6, "valid black decal selection for every box and anchor");
        }

        // Red-poster decals must be a fifth family, with a distinct, stable
        // encoded color index that cannot rewrite existing saved black layers.
        int red=DecalStackRules.pack(DecalStackRules.RED,3,4);
        check(DecalStackRules.supportedColor(DecalStackRules.RED),
                "red carpet only activates after its six art assets exist");
        check(DecalStackRules.artCount(DecalStackRules.RED)==6,
                "exactly six finished red posters");
        check(DecalStackRules.isValid(red)&&DecalStackRules.color(red)==4,
                "red uses a distinct fifth color encoding");
        check(!DecalStackRules.isValid(DecalStackRules.pack(DecalStackRules.RED,6,4)),
                "unpainted red art cannot be encoded");
        int oldBlack=DecalStackRules.pack(DecalStackRules.BLACK,5,4);
        int[] fiveMixed=DecalStackRules.append(
                DecalStackRules.append(
                        DecalStackRules.append(new int[0],white), oldBlack), red);
        check(Arrays.equals(fiveMixed,new int[]{white,oldBlack,red}),
                "red remains uppermost after old white+black layers");
        check(Arrays.equals(DecalStackRules.peel(fiveMixed),
                new int[]{white,oldBlack}),"peeling red leaves black intact");
        check(Arrays.equals(DecalStackRules.sanitized(fiveMixed),fiveMixed),
                "saved mixed-color ordering survives sanitation");
        check(DecalStackRules.append(fiveMixed,red).length==3,
                "red cannot bypass the existing three-layer cap");
        for(int variant=1;variant<=19;variant++)for(int slot=0;slot<9;slot++){
            int next=DecalStackRules.nextArt(DecalStackRules.RED,variant,2,slot);
            check(next>=0&&next<6,"every box/anchor selects one finished red poster");
        }

        // Orange uses a new encoded family, without modifying old red values.
        int orange=DecalStackRules.pack(DecalStackRules.ORANGE,2,4);
        check(DecalStackRules.supportedColor(DecalStackRules.ORANGE),
                "orange family is activated only with completed source art");
        check(DecalStackRules.artCount(DecalStackRules.ORANGE)==6,
                "orange has six original posters");
        check(DecalStackRules.isValid(orange) && DecalStackRules.color(orange)==5,
                "orange uses a new unique fifth-index family without colliding with red");
        check(!DecalStackRules.isValid(DecalStackRules.pack(DecalStackRules.ORANGE,6,4)),
                "no unpainted orange poster may be stored");
        int redOld=DecalStackRules.pack(DecalStackRules.RED,3,4);
        int[] orangeOnTop=DecalStackRules.append(
                DecalStackRules.append(DecalStackRules.append(
                    new int[0],white),redOld),orange);
        check(Arrays.equals(orangeOnTop,new int[]{white,redOld,orange}),
                "orange is newest in mixed white/red/orange stack");
        check(Arrays.equals(DecalStackRules.peel(orangeOnTop),new int[]{white,redOld}),
                "peeling orange preserves existing red and white data");
        check(Arrays.equals(DecalStackRules.sanitized(orangeOnTop),orangeOnTop),
                "mixed orange data survives serialization sanitizer");
        check(DecalStackRules.append(orangeOnTop,orange).length==3,
                "no fourth sticker even if it is orange");
        for(int variant=1;variant<=19;variant++)for(int slot=0;slot<9;slot++){
            int selected=DecalStackRules.nextArt(DecalStackRules.ORANGE,variant,2,slot);
            check(selected>=0&&selected<6,"every variant and anchor has a valid orange art");
        }
        System.out.println("PASS: "+count+"/"+count+" six-carpet decal stack, peeling and ID tests");
    }
}

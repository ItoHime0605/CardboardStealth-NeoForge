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
        for(int col=0;col<=11;col++)for(int art=0;art<6;art++)for(int anchor=0;anchor<9;anchor++) {
            int entry=DecalStackRules.pack(col,art,anchor);
            check(DecalStackRules.isValid(entry),"all real art codes are valid");
            check(DecalStackRules.color(entry)==col,"carpet color survives packing");
            check(DecalStackRules.art(entry)==art,"art number survives packing");
            check(DecalStackRules.anchor(entry)==anchor,"location survives packing");
        }
        for(int col=12;col<16;col++){
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

        // Keep every pre-existing color code stable when adding yellow.
        int yellow=DecalStackRules.pack(DecalStackRules.YELLOW,1,4);
        check(DecalStackRules.supportedColor(DecalStackRules.YELLOW),
                "only finished yellow hazards are allowed");
        check(DecalStackRules.artCount(DecalStackRules.YELLOW)==6,
                "yellow has exactly six original drawings");
        check(DecalStackRules.isValid(yellow) &&
                DecalStackRules.color(yellow)==6,
                "yellow color ID is the non-colliding seventh index");
        check(!DecalStackRules.isValid(
                DecalStackRules.pack(DecalStackRules.YELLOW,6,4)),
                "unpainted yellow hazards must never become invisible layers");
        int oldOrange=DecalStackRules.pack(DecalStackRules.ORANGE,2,4);
        int[] mixedYellow=DecalStackRules.append(
                DecalStackRules.append(
                DecalStackRules.append(new int[0],white),oldOrange),yellow);
        check(Arrays.equals(mixedYellow,new int[]{white,oldOrange,yellow}),
                "new yellow remains the topmost among mixed poster families");
        check(Arrays.equals(DecalStackRules.peel(mixedYellow),new int[]{white,oldOrange}),
                "yellow peels cleanly without changing prior orange art");
        check(Arrays.equals(DecalStackRules.sanitized(mixedYellow),mixedYellow),
                "mixed saved yellow layers survive sanitizer");
        check(DecalStackRules.append(mixedYellow,yellow).length==3,
                "fourth decal remains rejected for yellow family");
        for(int variant=1;variant<=19;variant++) for(int slot=0;slot<9;slot++){
            int selected=DecalStackRules.nextArt(DecalStackRules.YELLOW,variant,2,slot);
            check(selected>=0 && selected<6,
                    "all cardboard variants and 9 anchors select a valid yellow drawing");
        }

        // Lime is family #8 and must not rewrite any old colored decoration.
        int lime=DecalStackRules.pack(DecalStackRules.LIME,4,4);
        check(DecalStackRules.supportedColor(DecalStackRules.LIME),
                "lime only activates when six botanical paintings are available");
        check(DecalStackRules.artCount(DecalStackRules.LIME)==6,
                "exactly six new lime drawings");
        check(DecalStackRules.isValid(lime)&&DecalStackRules.color(lime)==7,
                "lime uses a unique eighth stored color code");
        check(!DecalStackRules.isValid(
                DecalStackRules.pack(DecalStackRules.LIME,6,4)),
                "invalid lime index is not a stored invisible decal");
        int savedYellow=DecalStackRules.pack(DecalStackRules.YELLOW,1,4);
        int[] eightMixed=DecalStackRules.append(DecalStackRules.append(
                DecalStackRules.append(new int[0],white),
                savedYellow),lime);
        check(Arrays.equals(eightMixed,new int[]{white,savedYellow,lime}),
                "new lime layer stays on top without changing older art");
        check(Arrays.equals(DecalStackRules.peel(eightMixed),
                new int[]{white,savedYellow}),
                "peeling lime keeps old yellow and white art byte-for-byte");
        check(Arrays.equals(DecalStackRules.sanitized(eightMixed),eightMixed),
                "saved three-color stack survives decoding");
        check(DecalStackRules.append(eightMixed,lime).length==3,
                "no fourth lime decoration allowed");
        for(int variant=1;variant<=19;variant++)for(int slot=0;slot<9;slot++){
            int chosen=DecalStackRules.nextArt(DecalStackRules.LIME,variant,2,slot);
            check(chosen>=0 && chosen<6,
                "every paper-box variant and attachment anchor selects botanical art");
        }

        // Ninth color expands family support without touching saved 0..7 tags.
        int pink=DecalStackRules.pack(DecalStackRules.PINK,5,4);
        check(DecalStackRules.supportedColor(DecalStackRules.PINK),
                "pink only enabled after six graphics are implemented");
        check(DecalStackRules.artCount(DecalStackRules.PINK)==6,
                "six different pink street-culture artworks");
        check(DecalStackRules.isValid(pink)&&DecalStackRules.color(pink)==8,
                "pink has independent ninth encoded color index");
        check(!DecalStackRules.isValid(DecalStackRules.pack(DecalStackRules.PINK,6,4)),
                "unpainted pink sprite index rejected, not silently invisible");
        int savedLime=DecalStackRules.pack(DecalStackRules.LIME,4,4);
        int[] pinkOnTop=DecalStackRules.append(
                DecalStackRules.append(DecalStackRules.append(new int[0],
                white),savedLime),pink);
        check(Arrays.equals(pinkOnTop,new int[]{white,savedLime,pink}),
                "old white and lime art retained beneath the pink sticker");
        check(Arrays.equals(DecalStackRules.peel(pinkOnTop),
                new int[]{white,savedLime}),
                "slime peels only new pink layer, not earlier decals");
        check(Arrays.equals(DecalStackRules.sanitized(pinkOnTop),pinkOnTop),
                "saved mixed pink color set passes data sanitization");
        check(DecalStackRules.append(pinkOnTop,pink).length==3,
                "fourth pink sticker cannot bypass original three-layer cap");
        for(int variant=1;variant<=19;variant++)for(int slot=0;slot<9;slot++){
            int art=DecalStackRules.nextArt(DecalStackRules.PINK,variant,2,slot);
            check(art>=0 && art<6,
                  "all paperbox variants and anchors select a real pink illustration");
        }

        // All pre-existing family codes remain stable when adding brown=9.
        int brown = DecalStackRules.pack(DecalStackRules.BROWN,5,4);
        check(DecalStackRules.supportedColor(DecalStackRules.BROWN),
                "brown carpet enabled only after six woodcut artworks exist");
        check(DecalStackRules.artCount(DecalStackRules.BROWN)==6,
                "exactly six independent brown survival illustrations");
        check(DecalStackRules.isValid(brown) && DecalStackRules.color(brown)==9,
                "brown uses the tenth non-colliding saved family code");
        check(!DecalStackRules.isValid(
                DecalStackRules.pack(DecalStackRules.BROWN,6,4)),
                "unsupported brown art must not become an invisible layer");
        int oldPink = DecalStackRules.pack(DecalStackRules.PINK,5,4);
        int[] brownMixed = DecalStackRules.append(
                DecalStackRules.append(
                DecalStackRules.append(new int[0],white),oldPink),brown);
        check(Arrays.equals(brownMixed,new int[]{white,oldPink,brown}),
                "brown is uppermost, white and pink remain byte-stable");
        check(Arrays.equals(DecalStackRules.peel(brownMixed),
                new int[]{white,oldPink}),
                "slime peels only new brown layer, not saved pink");
        check(Arrays.equals(DecalStackRules.sanitized(brownMixed),brownMixed),
                "oldest to newest order preserved after sanitizing");
        check(DecalStackRules.append(brownMixed,brown).length==3,
                "no fourth layer with brown carpet");
        for(int variant=1;variant<=19;variant++)for(int slot=0;slot<9;slot++){
            int art = DecalStackRules.nextArt(DecalStackRules.BROWN,variant,2,slot);
            check(art>=0 && art<6,
                    "every box variant and each anchor selects a brown artwork");
        }

        // The eleventh decal family gets a fresh color index, no old IDs move.
        int green=DecalStackRules.pack(DecalStackRules.GREEN,5,4);
        check(DecalStackRules.supportedColor(DecalStackRules.GREEN),
                "finished green family is supported");
        check(DecalStackRules.artCount(DecalStackRules.GREEN)==6,
                "exactly six finished green outpost drawings");
        check(DecalStackRules.isValid(green) && DecalStackRules.color(green)==10,
                "green uses reserved color 10, previously saved 0..9 unchanged");
        check(!DecalStackRules.isValid(DecalStackRules.pack(DecalStackRules.GREEN,6,4)),
                "unknown green decal art rejected without invisible layer");
        int savedBrown=DecalStackRules.pack(DecalStackRules.BROWN,5,4);
        int[] three=DecalStackRules.append(
            DecalStackRules.append(
            DecalStackRules.append(new int[0],white),savedBrown),green);
        check(Arrays.equals(three,new int[]{white,savedBrown,green}),
                "new green decal lies above old white and brown layers");
        check(Arrays.equals(DecalStackRules.peel(three),new int[]{white,savedBrown}),
                "peeling green leaves old brown/white data untouched");
        check(Arrays.equals(DecalStackRules.sanitized(three),three),
                "old and new decal order survives sanitation");
        check(DecalStackRules.append(three,green).length==3,
                "fourth green layer still rejected");
        for(int variant=1;variant<=19;variant++)for(int slot=0;slot<9;slot++){
            int chosen=DecalStackRules.nextArt(DecalStackRules.GREEN,variant,2,slot);
            check(chosen>=0&&chosen<6,
                  "all cardboard variants and nine anchors select completed green print");
        }

        // Cyan is saved as a new color code: previously existing 0..10 untouched.
        int cyan=DecalStackRules.pack(DecalStackRules.CYAN,5,4);
        check(DecalStackRules.supportedColor(DecalStackRules.CYAN),
                "only finished cyan weather designs can be used");
        check(DecalStackRules.artCount(DecalStackRules.CYAN)==6,
                "exactly six cyan weather observatory diagrams");
        check(DecalStackRules.isValid(cyan) && DecalStackRules.color(cyan)==11,
                "cyan receives next independent encoded color without colliding");
        check(!DecalStackRules.isValid(DecalStackRules.pack(DecalStackRules.CYAN,6,4)),
                "missing cyan art cannot become an invisible saved layer");
        int originalGreen=DecalStackRules.pack(DecalStackRules.GREEN,5,4);
        int[] mixed=DecalStackRules.append(
                DecalStackRules.append(
                DecalStackRules.append(new int[0],white),originalGreen),cyan);
        check(Arrays.equals(mixed,new int[]{white,originalGreen,cyan}),
                "cyan lies above saved white and green in paint order");
        check(Arrays.equals(DecalStackRules.peel(mixed),
                new int[]{white,originalGreen}),
                "peeling cyan preserves the older green and white data");
        check(Arrays.equals(DecalStackRules.sanitized(mixed),mixed),
                "three mixed save entries survive decoding");
        check(DecalStackRules.append(mixed,cyan).length==3,
                "cyan cannot bypass the existing three-layer cap");
        for(int variant=1;variant<=19;variant++)for(int slot=0;slot<9;slot++){
            int chosen=DecalStackRules.nextArt(DecalStackRules.CYAN,variant,2,slot);
            check(chosen>=0&&chosen<6,
                  "every cardboard variant and crafting anchor picks a real cyan image");
        }
        System.out.println("PASS: "+count+"/"+count+" twelve-carpet decal stack, peeling and ID tests");
    }
}

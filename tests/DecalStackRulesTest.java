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
        for(int col=0;col<=1;col++)for(int art=0;art<6;art++)for(int anchor=0;anchor<9;anchor++) {
            int entry=DecalStackRules.pack(col,art,anchor);
            check(DecalStackRules.isValid(entry),"all real art codes are valid");
            check(DecalStackRules.color(entry)==col,"carpet color survives packing");
            check(DecalStackRules.art(entry)==art,"art number survives packing");
            check(DecalStackRules.anchor(entry)==anchor,"location survives packing");
        }
        for(int col=2;col<16;col++){
            check(!DecalStackRules.supportedColor(col),"unfinished carpet not implemented yet");
            check(!DecalStackRules.isValid(DecalStackRules.pack(col,0,0)),
                    "unsupported art rejected, no blank texture");
        }
        for(int v=1;v<=19;v++)for(int slot=0;slot<9;slot++){
            int art=DecalStackRules.nextArt(DecalStackRules.LIGHT_GRAY,v,1,slot);
            check(art>=0 && art<6,"light gray art is from the six finished stickers");
        }
        System.out.println("PASS: "+count+"/"+count+" mixed decal stack, peeling, and art palette tests");
    }
}

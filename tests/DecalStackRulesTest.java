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
        System.out.println("PASS: "+count+"/"+count+" decal stack and layer-order tests");
    }
}

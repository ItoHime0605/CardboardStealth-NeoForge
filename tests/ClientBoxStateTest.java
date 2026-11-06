import tw.zonezero.cardboardstealth.client.ClientBoxState;

public final class ClientBoxStateTest {
    private static int checks;
    private static void check(boolean valid,String message){
        checks++;
        if(!valid)throw new AssertionError(message);
    }
    public static void main(String[] args){
        ClientBoxState.clear();
        ClientBoxState.receive(17,1,false,-1,-1,-1);
        ClientBoxState.receive(18,12,true,-1,-1,-1);
        check(ClientBoxState.variant(17)==1,"one boxed player stored");
        check(ClientBoxState.variant(18)==12,"second boxed player stored separately");
        ClientBoxState.receive(17,0,false,-1,-1,-1);
        check(ClientBoxState.variant(17)==0,"removing box immediately unhides SKIN");
        check(ClientBoxState.variant(18)==12,"unboxing one player does not affect other");
        ClientBoxState.receive(17,2,true,-1,-1,-1);
        ClientBoxState.forget(17);
        check(ClientBoxState.variant(17)==0,"entity leaving level discards stale disguise");
        check(ClientBoxState.variant(18)==12,"other entity keeps its own disguise");
        check(!ClientBoxState.rested(17),"forgotten entity has no old motion state");
        check(ClientBoxState.decals(17).length==0,"forgotten entity has no decals");
        ClientBoxState.receive(17,0,false,-1,-1,-1);
        check(ClientBoxState.variant(17)==0,"reused entity ID starts unboxed");
        ClientBoxState.clear();
        check(ClientBoxState.variant(18)==0,"logout clears all client visual state");
        System.out.println("PASS: "+checks+"/"+checks+
                " client state transitions, safe entity reuse and isolation tests");
    }
}

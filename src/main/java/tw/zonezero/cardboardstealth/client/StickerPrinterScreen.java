package tw.zonezero.cardboardstealth.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import tw.zonezero.cardboardstealth.CardboardStealth;
import tw.zonezero.cardboardstealth.ModItems;
import tw.zonezero.cardboardstealth.StickerPrinterMenu;
import tw.zonezero.cardboardstealth.StickerPrinterRules;

/**
 * Industrial machine GUI: large true-art proof print, six numbered presses,
 * two inputs, output tray, centered machine status, and vanilla player inventory.
 * All choices use standard container button packets; dye is validated on server.
 */
public final class StickerPrinterScreen extends AbstractContainerScreen<StickerPrinterMenu> {
    private static final int WIDTH=244,HEIGHT=243;
    private static final int FRAME=0xFF101A21,EDGE=0xFF708695;
    private static final int PANEL=0xFF263842,INTERIOR=0xFF172630;
    private static final int TEXT=0xFFE9E8D6,AMBER=0xFFE7AF62;
    private Button printButton;

    private static final int[] DYE_COLORS={
        0xFFE6E8D8,0xFFB9BBC4,0xFF61656D,0xFF292C34,
        0xFFB84747,0xFFF39547,0xFFE5D35C,0xFF87C965,
        0xFFF49DB9,0xFF815E46,0xFF4F9B5C,0xFF4DA7AB,
        0xFF4575BB,0xFF9362B4,0xFFBC6AB8,0xFF83C5EB
    };

    public StickerPrinterScreen(StickerPrinterMenu menu,Inventory inventory,Component title){
        super(menu,inventory,title);
        imageWidth=WIDTH; imageHeight=HEIGHT;
        titleLabelX=12; titleLabelY=10;
        inventoryLabelX=36; inventoryLabelY=147;
    }
    @Override protected void init(){
        super.init();
        for(int i=0;i<6;i++){
            final int pattern=i;
            addRenderableWidget(Button.builder(Component.literal(Integer.toString(i+1)),button->{
                if(minecraft!=null&&minecraft.gameMode!=null)
                    minecraft.gameMode.handleInventoryButtonClick(menu.containerId,pattern);
            }).bounds(leftPos+73+i*26,topPos+97,23,19).build());
        }
        printButton=addRenderableWidget(Button.builder(
                Component.translatable("gui.cardboardstealth.printer.print"),button->{
                    if(minecraft!=null&&minecraft.gameMode!=null)
                        minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                                StickerPrinterMenu.PRINT);
                }).bounds(leftPos+80,topPos+121,139,20).build());
    }
    @Override protected void containerTick(){
        super.containerTick();
        if(printButton!=null)printButton.active=menu.printEnabled();
    }
    private static void plate(GuiGraphics g,int x,int y,int w,int h,int outer,int inner){
        g.fill(x,y,x+w,y+h,outer);
        g.fill(x+2,y+2,x+w-2,y+h-2,inner);
    }
    @Override protected void renderBg(GuiGraphics g,float delta,int mx,int my){
        int x=leftPos,y=topPos;
        g.fillGradient(x,y,x+WIDTH,y+HEIGHT,0xFF53646B,FRAME);
        g.fill(x+2,y+2,x+WIDTH-2,y+HEIGHT-2,EDGE);
        g.fillGradient(x+4,y+4,x+WIDTH-4,y+HEIGHT-4,
                0xFF354650,0xFF1A2933);
        // Raised steel control strip and luminous stripe.
        plate(g,x+8,y+7,WIDTH-16,17,FRAME,PANEL);
        g.fill(x+9,y+24,x+WIDTH-9,y+26,AMBER);
        g.fill(x+9,y+27,x+WIDTH-9,y+28,0xFF0D1921);
        // Input bays, output tray and illuminated proof-print display.
        plate(g,x+15,y+36,61,54,FRAME,INTERIOR);
        plate(g,x+202,y+36,28,54,FRAME,INTERIOR);
        plate(g,x+80,y+30,116,62,0xFF0B151D,0xFF152831);
        g.fill(x+84,y+34,x+192,y+87,0xFF121E26);
        for(int dy=38;dy<85;dy+=5)
            g.fill(x+86,y+dy,x+190,y+dy+1,0xFF253741);
        // Source sticker art, cropped from the original shared 256x240 atlas.
        int color=menu.color(),art=menu.pattern();
        if(color>=0&&color<16&&art>=0&&art<StickerPrinterRules.PATTERNS){
            String name=ModItems.STICKER_COLORS[color];
            ResourceLocation atlas=ResourceLocation.fromNamespaceAndPath(
                    CardboardStealth.MOD_ID,"textures/decals/"+name+"_atlas.png");
            g.blit(atlas,x+90,y+40,96,44,(art%2)*128F,(art/2)*80F,
                    128,80,256,240);
            g.fill(x+15,y+34,x+75,y+37,DYE_COLORS[color]);
        }else{
            g.drawCenteredString(font,Component.translatable("gui.cardboardstealth.printer.no_dye"),x+138,y+58,0xFF879CA2);
            g.fill(x+15,y+34,x+75,y+37,0xFF6C7A80);
        }
        // Thin output-tray mouth and six preview selection guides.
        g.fill(x+204,y+80,x+229,y+83,0xFF0A1217);
        for(int i=0;i<6;i++){
            int px=x+73+i*26;
            g.fill(px,y+117,px+23,y+119,
                    i==menu.pattern()?AMBER:0xFF3F5461);
        }
        g.fill(x+8,y+145,x+WIDTH-8,y+146,0xFF87969A);
        // Standard Minecraft inventory slots, surrounded by inset machine panel.
        plate(g,x+28,y+152,180,82,0xFF0D181D,0xFF26343A);
        for(int row=0;row<3;row++)for(int col=0;col<9;col++){
            int sx=x+35+col*18,sy=y+157+row*18;
            g.fill(sx,sy,sx+18,sy+18,0xFF080F14);
            g.fill(sx+1,sy+1,sx+17,sy+17,0xFF657477);
            g.fill(sx+2,sy+2,sx+16,sy+16,0xFF202D33);
        }
        for(int col=0;col<9;col++){
            int sx=x+35+col*18,sy=y+215;
            g.fill(sx,sy,sx+18,sy+18,0xFF080F14);
            g.fill(sx+1,sy+1,sx+17,sy+17,0xFF657477);
            g.fill(sx+2,sy+2,sx+16,sy+16,0xFF202D33);
        }
        // Actual Slot and contained ItemStacks render above these frames.
        for(int sx : new int[]{24,50,210}){
            g.fill(x+sx-1,y+51,x+sx+17,y+69,0xFF88949B);
            g.fill(x+sx,y+52,x+sx+16,y+68,0xFF202D32);
        }
    }
    @Override protected void renderLabels(GuiGraphics g,int mx,int my){
        g.drawString(font,title,12,10,TEXT,false);
        g.drawString(font,Component.translatable("gui.cardboardstealth.printer.blank"),
                16,76,0xFFC8D5D7,false);
        g.drawString(font,Component.translatable("gui.cardboardstealth.printer.dye"),
                49,76,0xFFC8D5D7,false);
        g.drawString(font,Component.translatable("gui.cardboardstealth.printer.output"),
                199,76,0xFFC8D5D7,false);
        int color=menu.color(),art=menu.pattern();
        Component name=color<0
                ?Component.translatable("gui.cardboardstealth.printer.no_dye")
                :Component.translatable("item.cardboardstealth.sticker_subject_"+
                        ModItems.STICKER_COLORS[color]+"_"+(art+1));
        String shortName=font.plainSubstrByWidth(name.getString(),114);
        g.drawString(font,shortName,82,87,0xFFE4E1D0,false);
        String status=color<0
                ?Component.translatable("gui.cardboardstealth.printer.insert_dye").getString()
                :Component.translatable("gui.cardboardstealth.printer.cost",
                        StickerPrinterRules.requiredDye(art),menu.dyeCount()).getString();
        g.drawString(font,font.plainSubstrByWidth(status,116),82,32,AMBER,false);
        g.drawString(font,playerInventoryTitle,36,147,TEXT,false);
    }
    @Override public void render(GuiGraphics g,int mouseX,int mouseY,float partialTick){
        renderBackground(g,mouseX,mouseY,partialTick);
        super.render(g,mouseX,mouseY,partialTick);
        renderTooltip(g,mouseX,mouseY);
        // Disabled printing should say WHY: missing paper, short dye count,
        // no dye, or an occupied output. No guessing in the machine GUI.
        int localX=mouseX-leftPos,localY=mouseY-topPos;
        if(localX>=80&&localX<219&&localY>=121&&localY<141){
            g.renderTooltip(font,Component.translatable(menu.printStatusKey()),
                    mouseX,mouseY);
        }
        // A Chinese design title may be longer than the miniature monitor.
        // Hovering the live proof print OR any numbered key reveals the
        // complete illustrated subject, never an ellipsis-only name.
        int rx=mouseX-leftPos,ry=mouseY-topPos;
        int selected=menu.pattern();
        if(ry>=97&&ry<=116&&rx>=73&&rx<73+6*26){
            int candidate=(rx-73)/26;
            if((rx-73)%26<23)selected=candidate;
            else selected=-1;
        }else if(!(rx>=80&&rx<=197&&ry>=30&&ry<=92)){
            selected=-1;
        }
        int color=menu.color();
        if(color>=0&&color<16&&selected>=0&&selected<6){
            g.renderTooltip(font,Component.translatable(
                    "item.cardboardstealth.sticker_subject_"+
                    ModItems.STICKER_COLORS[color]+"_"+(selected+1)),
                    mouseX,mouseY);
        }
    }
}

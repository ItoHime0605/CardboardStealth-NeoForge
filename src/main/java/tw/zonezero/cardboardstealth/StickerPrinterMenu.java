package tw.zonezero.cardboardstealth;

import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

/** Server-authoritative controls, synchronized through vanilla menu packets. */
public final class StickerPrinterMenu extends AbstractContainerMenu {
    public static final int PRINT=100;
    private final Container inventory;
    private final ContainerData data;
    private final ContainerLevelAccess access;
    private final StickerPrinterBlockEntity machine;

    public StickerPrinterMenu(int containerId,Inventory playerInventory){
        this(containerId,playerInventory,new SimpleContainer(StickerPrinterBlockEntity.SLOTS),
                new SimpleContainerData(1),ContainerLevelAccess.NULL,null);
    }
    public StickerPrinterMenu(int containerId,Inventory playerInventory,
                              StickerPrinterBlockEntity machine){
        this(containerId,playerInventory,machine,machine.data,
                ContainerLevelAccess.create(machine.getLevel(),machine.getBlockPos()),machine);
    }
    private StickerPrinterMenu(int id,Inventory playerInventory,Container inventory,
             ContainerData data,ContainerLevelAccess access,StickerPrinterBlockEntity machine){
        super(ModMachines.PRINTER_MENU.get(),id);
        checkContainerSize(inventory,3);
        checkContainerDataCount(data,1);
        this.inventory=inventory;
        this.data=data;
        this.access=access;
        this.machine=machine;
        addSlot(new Slot(inventory,0,24,52){
            @Override public boolean mayPlace(ItemStack stack){
                return stack.is(ModItems.BLANK_STICKER.get());
            }
        });
        addSlot(new Slot(inventory,1,50,52){
            @Override public boolean mayPlace(ItemStack stack){
                return StickerPrinterBlockEntity.dyeColor(stack)>=0;
            }
        });
        addSlot(new Slot(inventory,2,210,52){
            @Override public boolean mayPlace(ItemStack stack){return false;}
        });
        // Vanilla inventory (3 rows) and hotbar, at fixed screen positions.
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)
            addSlot(new Slot(playerInventory,col+row*9+9,
                    36+col*18,158+row*18));
        for(int col=0;col<9;col++)
            addSlot(new Slot(playerInventory,col,36+col*18,216));
        addDataSlots(data);
    }

    public int pattern(){return data.get(0);}
    public int color(){return StickerPrinterBlockEntity.dyeColor(inventory.getItem(1));}
    public int dyeCount(){return inventory.getItem(1).getCount();}
    public boolean printEnabled(){
        return StickerPrinterRules.canPrint(
                inventory.getItem(0).is(ModItems.BLANK_STICKER.get())
                        ? inventory.getItem(0).getCount():0,
                dyeCount(),color(),pattern(),inventory.getItem(2).isEmpty());
    }
    @Override public boolean clickMenuButton(Player player,int id){
        if(machine==null || !stillValid(player))return false;
        if(id>=0&&id<6){
            machine.choosePattern(id);
            broadcastChanges();
            return true;
        }
        if(id==PRINT&&machine.print()){
            broadcastChanges();
            return true;
        }
        return false;
    }
    @Override public ItemStack quickMoveStack(Player player,int index){
        if(index<0||index>=slots.size())return ItemStack.EMPTY;
        Slot clicked=slots.get(index);
        if(!clicked.hasItem())return ItemStack.EMPTY;
        ItemStack source=clicked.getItem();
        ItemStack original=source.copy();
        if(index<3){
            if(!moveItemStackTo(source,3,39,true))return ItemStack.EMPTY;
        } else if(source.is(ModItems.BLANK_STICKER.get())){
            if(!moveItemStackTo(source,0,1,false))return ItemStack.EMPTY;
        } else if(StickerPrinterBlockEntity.dyeColor(source)>=0){
            if(!moveItemStackTo(source,1,2,false))return ItemStack.EMPTY;
        } else if(index<30){
            if(!moveItemStackTo(source,30,39,false))return ItemStack.EMPTY;
        } else if(!moveItemStackTo(source,3,30,false)){
            return ItemStack.EMPTY;
        }
        if(source.isEmpty())clicked.set(ItemStack.EMPTY);
        else clicked.setChanged();
        if(source.getCount()==original.getCount())return ItemStack.EMPTY;
        clicked.onTake(player,source);
        return original;
    }
    @Override public boolean stillValid(Player player){
        return machine==null || machine.stillValid(player);
    }
    @Override public void removed(Player player){
        super.removed(player);
        inventory.stopOpen(player);
        // Deliberately keep materials/output in block entity until emptied
        // or until the block is destroyed (then it drops its contents).
    }
}

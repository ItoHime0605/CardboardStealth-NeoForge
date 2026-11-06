package tw.zonezero.cardboardstealth;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/**
 * Durable, server-authoritative 3-slot sticker printer.
 * 0: blank sticker; 1: one color of vanilla dye; 2: finished product.
 * No ticking, electric energy or ongoing per-player polling.
 */
public final class StickerPrinterBlockEntity extends BlockEntity
        implements Container, net.minecraft.world.MenuProvider {
    public static final int BLANK=0,DYE=1,OUTPUT=2,SLOTS=3;
    private final NonNullList<ItemStack> inventory=
            NonNullList.withSize(SLOTS,ItemStack.EMPTY);
    private int pattern=0;
    public final ContainerData data=new ContainerData(){
        @Override public int get(int index){return index==0?pattern:0;}
        @Override public void set(int index,int value){
            if(index==0)choosePattern(value);
        }
        @Override public int getCount(){return 1;}
    };

    public StickerPrinterBlockEntity(BlockPos pos, BlockState state) {
        super(ModMachines.PRINTER_ENTITY.get(),pos,state);
    }

    public static int dyeColor(ItemStack stack) {
        if(!(stack.getItem() instanceof DyeItem item))return -1;
        String color=item.getDyeColor().getName();
        for(int i=0;i<ModItems.STICKER_COLORS.length;i++)
            if(ModItems.STICKER_COLORS[i].equals(color))return i;
        return -1;
    }

    public int selectedPattern(){return pattern;}
    public void choosePattern(int value){
        if(value<0||value>=StickerPrinterRules.PATTERNS)return;
        pattern=value;
        setChanged();
    }
    public boolean canPrint(){
        return StickerPrinterRules.canPrint(
                getItem(BLANK).is(ModItems.BLANK_STICKER.get())
                    ?getItem(BLANK).getCount():0,
                getItem(DYE).getCount(),dyeColor(getItem(DYE)),pattern,
                getItem(OUTPUT).isEmpty());
    }
    /** Must only be called in the server menu click callback. */
    public boolean print(){
        if(level==null||level.isClientSide||level.getBlockEntity(worldPosition)!=this||!canPrint())return false;
        int color=dyeColor(getItem(DYE));
        int cost=StickerPrinterRules.requiredDye(pattern);
        String id=StickerPrinterRules.outputId(ModItems.STICKER_COLORS,color,pattern);
        var item=ModItems.STICKERS.get(id);
        if(item==null)return false;
        // All requirements checked before any irreversible consumption.
        ItemStack finished=new ItemStack(item.get());
        removeItem(BLANK,1);
        removeItem(DYE,cost);
        setItem(OUTPUT,finished);
        setChanged();
        return true;
    }

    @Override public Component getDisplayName(){
        return Component.translatable("block.cardboardstealth.sticker_printer");
    }
    @Nullable @Override
    public AbstractContainerMenu createMenu(int id, Inventory playerInventory, Player player) {
        return new StickerPrinterMenu(id,playerInventory,this);
    }

    @Override protected void saveAdditional(CompoundTag tag,HolderLookup.Provider registries){
        super.saveAdditional(tag,registries);
        ContainerHelper.saveAllItems(tag,inventory,registries);
        tag.putInt("SelectedPattern",pattern);
    }
    @Override protected void loadAdditional(CompoundTag tag,HolderLookup.Provider registries){
        super.loadAdditional(tag,registries);
        // NonNullList.withSize(3, EMPTY).clear() RESETS the existing three
        // slots; it does not shrink the list. Adding slots after clear()
        // silently grew this inventory to six entries on every load.
        // ContainerHelper already resets and reloads the fixed-size list.
        ContainerHelper.loadAllItems(tag,inventory,registries);
        pattern=Math.clamp(tag.getInt("SelectedPattern"),0,5);
    }

    @Override public int getContainerSize(){return SLOTS;}
    @Override public boolean isEmpty(){return inventory.stream().allMatch(ItemStack::isEmpty);}
    @Override public ItemStack getItem(int slot){return inventory.get(slot);}
    @Override public ItemStack removeItem(int slot,int count){
        ItemStack taken=ContainerHelper.removeItem(inventory,slot,count);
        if(!taken.isEmpty())setChanged();
        return taken;
    }
    @Override public ItemStack removeItemNoUpdate(int slot){
        ItemStack removed=ContainerHelper.takeItem(inventory,slot);
        if(!removed.isEmpty())setChanged();
        return removed;
    }
    @Override public void setItem(int slot,ItemStack stack){
        inventory.set(slot,stack);
        if(stack.getCount()>getMaxStackSize())stack.setCount(getMaxStackSize());
        setChanged();
    }
    @Override public boolean canPlaceItem(int slot,ItemStack stack){
        return switch(slot){
            case BLANK -> stack.is(ModItems.BLANK_STICKER.get());
            case DYE -> dyeColor(stack)>=0;
            default -> false;
        };
    }
    @Override public boolean stillValid(Player player){
        // Reject late, forged or stale GUI packets after the physical machine
        // has been broken/replaced. Being near the old coordinates is not enough.
        return level!=null && level.getBlockEntity(worldPosition)==this
                && level.getBlockState(worldPosition).is(ModMachines.PRINTER.get())
                && player.distanceToSqr(worldPosition.getX()+0.5,
                        worldPosition.getY()+0.5,worldPosition.getZ()+0.5)<=64.0;
    }
    @Override public void clearContent(){
        for(int i=0;i<SLOTS;i++)inventory.set(i,ItemStack.EMPTY);
        setChanged();
    }
}

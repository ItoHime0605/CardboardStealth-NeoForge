package tw.zonezero.cardboardstealth;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** Precisely oriented, 1x1 stationary workshop printer. No chunk ticking. */
public final class StickerPrinterBlock extends BaseEntityBlock {
    public static final MapCodec<StickerPrinterBlock> CODEC=simpleCodec(StickerPrinterBlock::new);
    public static final DirectionProperty FACING=BlockStateProperties.HORIZONTAL_FACING;

    public StickerPrinterBlock(Properties properties){
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING,Direction.NORTH));
    }
    @Override protected MapCodec<? extends BaseEntityBlock> codec(){return CODEC;}
    @Override protected RenderShape getRenderShape(BlockState state){return RenderShape.MODEL;}
    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos,BlockState state){
        return new StickerPrinterBlockEntity(pos,state);
    }
    @Override public MenuProvider getMenuProvider(BlockState state,Level level,BlockPos pos){
        return level.getBlockEntity(pos) instanceof StickerPrinterBlockEntity printer
                ?printer:null;
    }
    @Override protected InteractionResult useWithoutItem(BlockState state,Level level,
            BlockPos pos,Player player,BlockHitResult hit){
        if(!level.isClientSide && player instanceof ServerPlayer server){
            if(level.getBlockEntity(pos) instanceof StickerPrinterBlockEntity printer)
                server.openMenu(printer);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
    @Override protected void onRemove(BlockState state,Level level,BlockPos pos,
                                      BlockState next,boolean moved){
        if(!state.is(next.getBlock())){
            if(level.getBlockEntity(pos) instanceof StickerPrinterBlockEntity printer)
                Containers.dropContents(level,pos,printer);
        }
        super.onRemove(state,level,pos,next,moved);
    }
    @Override public BlockState getStateForPlacement(BlockPlaceContext ctx){
        return defaultBlockState().setValue(FACING,ctx.getHorizontalDirection().getOpposite());
    }
    @Override protected BlockState rotate(BlockState state,Rotation rotation){
        return state.setValue(FACING,rotation.rotate(state.getValue(FACING)));
    }
    @Override protected BlockState mirror(BlockState state,Mirror mirror){
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<net.minecraft.world.level.block.Block,BlockState> builder){
        builder.add(FACING);
    }
}

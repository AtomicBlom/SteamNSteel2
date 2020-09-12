package com.github.steamnsteel.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.EnumProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;

public class PipeBlock extends Block {

    public static EnumProperty<ModelType> MODEL_TYPE = EnumProperty.create("type", ModelType.class);
    public static BooleanProperty[] CONNECTIONS;

    static {
        final Direction[] directions = Direction.values();
        CONNECTIONS = new BooleanProperty[directions.length];
        for (int index = 0; index < directions.length; index++) {
            CONNECTIONS[index] = BooleanProperty.create(directions[index].getName2());
        }
    }

    public PipeBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        //FIXME: Find a way to define the state container in a less wasteful manner

        builder.add(MODEL_TYPE);
        builder.add(CONNECTIONS);
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        final BlockPos pos = context.getPos();
        final World world = context.getWorld();
        final Direction nearestLookingDirection = context.getNearestLookingDirection();

        return getFullState(world, pos, nearestLookingDirection);
    }

    @Deprecated
    @Override
    public VoxelShape getShape(BlockState state, IBlockReader worldIn, BlockPos pos, ISelectionContext context) {
        //FIXME: Do this properly.
        return VoxelShapes.create(0.2, 0.2, 0.2, 0.8, 0.8, 0.8);
    }

    @Deprecated
    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        //FIXME: DETERMINE one of the last used sides
        worldIn.setBlockState(pos, getFullState(worldIn, pos, Direction.NORTH));
    }

    private BlockState getFullState(World world, BlockPos pos, Direction nearestLookingDirection) {
        BlockState state = this.getDefaultState();

        int activeConnections = 0;
        Direction lastFoundDirection = null;
        for (Direction direction : Direction.values()) {
            boolean isConnectable = world.getBlockState(pos.offset(direction)).getBlock() instanceof PipeBlock;
            state = state.with(CONNECTIONS[direction.getIndex()], isConnectable);
            activeConnections += isConnectable ? 1 : 0;
            if (isConnectable) {
                lastFoundDirection = direction;
            }
        }

        switch (activeConnections) {
            case 0:
                state = state
                        .with(MODEL_TYPE, ModelType.STRAIGHT_TERMINUS)
                        .with(CONNECTIONS[nearestLookingDirection.getIndex()], true)
                        .with(CONNECTIONS[nearestLookingDirection.getOpposite().getIndex()], true);
                break;
            case 1:
                assert lastFoundDirection != null;
                state = state
                        .with(MODEL_TYPE, ModelType.STRAIGHT_TERMINUS);
                break;
            case 2:
                final boolean isNorthSouthConnected = state.get(CONNECTIONS[Direction.NORTH.getIndex()]) && state.get(CONNECTIONS[Direction.SOUTH.getIndex()]);
                final boolean isEastWestConnected = state.get(CONNECTIONS[Direction.EAST.getIndex()]) && state.get(CONNECTIONS[Direction.WEST.getIndex()]);
                final boolean isUpDownConnected = state.get(CONNECTIONS[Direction.DOWN.getIndex()]) && state.get(CONNECTIONS[Direction.UP.getIndex()]);

                if (isNorthSouthConnected || isEastWestConnected || isUpDownConnected) {
                    state = state.with(MODEL_TYPE, ModelType.STRAIGHT);
                } else {
                    state = state.with(MODEL_TYPE, ModelType.ELBOW);
                }
                break;
            default:
                state = state.with(MODEL_TYPE, ModelType.MULTI);
                break;
        }

        return state;
    }

}

package voxelum.summer.blocks;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropsBlock;
import net.minecraft.block.material.Material;
import net.minecraft.util.IItemProvider;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.world.IBlockReader;
import voxelum.summer.HotSummerMod;

public class TeaCropsBlock extends CropsBlock {
    public TeaCropsBlock(Properties builder) {
        super(builder);
        System.out.println(this.getMaterial(this.getDefaultState()) == Material.PLANTS);
        System.out.println(this.getMaterial(this.getDefaultState()).isOpaque());
        System.out.println(this.getMaterial(this.getDefaultState()).isSolid());
    }

    private static final VoxelShape[] SHAPES = new VoxelShape[]{
                    Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D),
                    Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 3.0D, 16.0D),
                    Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D),
                    Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 5.0D, 16.0D),
                    Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D),
                    Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 7.0D, 16.0D),
                    Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D),
                    Block.makeCuboidShape(0.0D, 0.0D, 0.0D, 16.0D, 9.0D, 16.0D)};

    public VoxelShape getShape(BlockState state, IBlockReader iblockreader, BlockPos pos, ISelectionContext what){
        return SHAPES[(Integer)state.get(this.getAgeProperty())];
    }

    @Override
    protected IItemProvider getSeedsItem() {
        return HotSummerMod.TEA_ITEM.get();
    }
}




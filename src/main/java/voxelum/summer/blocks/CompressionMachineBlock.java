package voxelum.summer.blocks;

import net.minecraft.block.ContainerBlock;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class CompressionMachineBlock extends ContainerBlock {
    public CompressionMachineBlock(Properties builder) {
        super(builder);
    }

    @Nullable
    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return null;
    }
}

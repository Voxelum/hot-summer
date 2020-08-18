package voxelum.summer.core.message;

import net.minecraft.util.math.BlockPos;

/**
 * The message to indicate a player try to drink water on the block pos.
 * The block pos is a liquid block.
 */
public class DrinkWaterMessage {
    public BlockPos pos;

    public static DrinkWaterMessage of(BlockPos pos) {
        DrinkWaterMessage message = new DrinkWaterMessage();
        message.pos = pos;
        return message;
    }
}

package voxelum.summer.blocks;

import net.minecraft.block.CropsBlock;
import net.minecraft.util.IItemProvider;
import voxelum.summer.HotSummerMod;

public class TeaCropsBlock extends CropsBlock {
    public TeaCropsBlock(Properties builder) {
        super(builder);
    }

    @Override
    protected IItemProvider getSeedsItem() {
        return HotSummerMod.TEA_SEEDS_ITEM.get();
    }
}

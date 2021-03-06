package voxelum.summer.gen.feature;

import com.mojang.datafixers.Dynamic;
import net.minecraft.block.BambooBlock;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.CropsBlock;
import net.minecraft.state.properties.BambooLeaves;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.gen.GenerationSettings;
import net.minecraft.world.gen.Heightmap;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraft.world.gen.feature.ProbabilityConfig;
import voxelum.summer.HotSummerMod;

import java.util.Random;
import java.util.function.Function;

public class TeaFeature extends Feature<NoFeatureConfig> {
    private static final BlockState TEA_BASE_STATE = HotSummerMod.TEA_CORP_BLOCK.get().getDefaultState().with(CropsBlock.AGE, 7);

    public TeaFeature() {
        super(NoFeatureConfig::deserialize);
    }

    @Override
    public boolean place(IWorld worldIn, ChunkGenerator<? extends GenerationSettings> generator, Random rand, BlockPos pos, NoFeatureConfig config) {
        int i = 0;
        BlockPos height = worldIn.getHeight(Heightmap.Type.WORLD_SURFACE_WG, pos);
        BlockState under = worldIn.getBlockState(height.add(0, -1, 0));
        if (worldIn.isAirBlock(height)
                && under.getBlock() == Blocks.GRASS_BLOCK) {
            worldIn.setBlockState(height, TEA_BASE_STATE, 2);
        }
        return false;
    }

}

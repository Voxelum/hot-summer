package voxelum.summer.core;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import voxelum.summer.HotSummerMod;

import java.util.List;

public class BodyAlgorithm {
//    private static final Object2IntAVLTreeMap<BlockState> BLOCK_HEAT_REG = new Object2IntAVLTreeMap<>();
//
//    static {
//        BLOCK_HEAT_REG.defaultReturnValue(0);
//        for (BlockState state : Blocks.LAVA.getStateContainer().getValidStates()) {
//            BLOCK_HEAT_REG.put(state, 100);
//        }
//    }

    static final int DISMISS_DIFF_FACTOR = 10;

    public static float transformBiomeTemperature(float biomeTemperature) {
        // You can calibrate temperatures using these
        // This does not take into account the time of day (These are the midday maximums)
        float maxTemp = 45F; // Desert
        float minTemp = -15F;
        float temp = (float) Math.toRadians(biomeTemperature * 45F);
        return biomeTemperature >= 0 ? MathHelper.sin(temp) * maxTemp : MathHelper.sin(temp) * minTemp;
    }

    public static float computeKeepWarmFactor(PlayerEntity playerEntity) {
        ItemStack head = playerEntity.getItemStackFromSlot(EquipmentSlotType.HEAD);
        ItemStack chest = playerEntity.getItemStackFromSlot(EquipmentSlotType.CHEST);
        ItemStack legs = playerEntity.getItemStackFromSlot(EquipmentSlotType.LEGS);
        ItemStack feet = playerEntity.getItemStackFromSlot(EquipmentSlotType.FEET);
        return 1
                + getKeepWarmFactor(head)
                + getKeepWarmFactor(chest)
                + getKeepWarmFactor(legs)
                + getKeepWarmFactor(feet);
    }

    public static float getKeepWarmFactor(ICapabilityProvider provider) {
        return provider.getCapability(HotSummerMod.CAPABILITY_WARN_KEEPER)
                .map((k) -> k.keepWarmFactor)
                .orElse(0F);
    }

    public static float getTemperature(ICapabilityProvider provider) {
        return provider.getCapability(HotSummerMod.CAPABILITY_HEAT_SOURCE)
                .map((k) -> k.temperature)
                .orElse(Float.MIN_VALUE);
    }

    @SuppressWarnings("unchecked")
    private static Tuple<Block, Integer>[] BUILT_IN_BLOCK_TEMPS = new Tuple[]{
            new Tuple<>(Blocks.LAVA, 100),
            new Tuple<>(Blocks.FIRE, 100),
            new Tuple<>(Blocks.ICE, -10),
    };

    public static float getTemperatureOfBlock(BlockState blockState) {
        Block block = blockState.getBlock();
        for (int i = 0; i < BUILT_IN_BLOCK_TEMPS.length; i++) {
            if (BUILT_IN_BLOCK_TEMPS[i].getA() == block) {
                return BUILT_IN_BLOCK_TEMPS[i].getB();
            }
        }
        if (block == Blocks.FURNACE) {
            boolean isLit = blockState.get(FurnaceBlock.LIT).booleanValue();
            if (isLit) {
                return 50;
            }
        }
        return Float.MIN_VALUE;
    }

    public static float getTemperatureShiftOnBlocks(LivingEntity origin, float sourceTemperature) {
        World world = origin.world;
        BlockPos.Mutable position = new BlockPos.Mutable(origin.getPosition());
        int cx = position.getX();
        int cy = position.getY();
        int cz = position.getZ();
        float total = 0;
        for (int x = -3; x < 3; ++x) {
            for (int y = -3; y < 3; y++) {
                for (int z = -3; z < 3; z++) {
                    BlockState state = world.getBlockState(position.setPos(x + cx, y + cy, z + cz));
                    float temperature = getTemperatureOfBlock(state);
                    if (temperature != Float.MIN_VALUE) {
                        // Manhattan Distance
                        int dis = Math.abs(x) + Math.abs(y) + Math.abs(z);
                        float factor = 1F / dis * 0.01F;
                        total += factor * (temperature - sourceTemperature + DISMISS_DIFF_FACTOR);
                    }
                }
            }
        }
        Debug.blockTemp = total;

        return total;
    }

    public static float getTemperatureShiftOnEntities(LivingEntity origin, float sourceTemperature) {
        World world = origin.world;

        float total = 0;
        List<Entity> others = world.getEntitiesWithinAABBExcludingEntity(origin, origin.getBoundingBox().expand(3, 3, 3));
        for (Entity other : others) {
            float temperature = getTemperature(other);
            if (temperature != Float.MIN_VALUE) {
                BlockPos src = other.getPosition();
                BlockPos des = origin.getPosition();
                int dis = Math.abs(src.getX() - des.getX())
                        + Math.abs(src.getY() - des.getY())
                        + Math.abs(src.getZ() - des.getZ());
                float factor = 1 - 0.05F * dis;
                total += factor * (temperature - sourceTemperature + DISMISS_DIFF_FACTOR);
            }
        }
        Debug.entityTemp = total;

        return total;
    }

    public static float getTemperatureShiftOnBiome(LivingEntity origin, float sourceTemperature) {
        BlockPos position = origin.getPosition();
        World world = origin.world;
        Biome biome = world.getBiome(position);
        float biomeTemperature = transformBiomeTemperature(biome.getTemperature(position));

        Debug.biomeTemp = biomeTemperature;

        return biomeTemperature - sourceTemperature + DISMISS_DIFF_FACTOR;
    }

    public static float getPassiveTemperatureShift(LivingEntity origin, float sourceTemperature) {
        float biomeTemperature = getTemperatureShiftOnBiome(origin, sourceTemperature);
        float blockTemperature = getTemperatureShiftOnBlocks(origin, sourceTemperature);
        float entitiesTemperature = getTemperatureShiftOnEntities(origin, sourceTemperature);

        return biomeTemperature
                + blockTemperature
                + entitiesTemperature;
    }

    public static void updateBodyStatus(PlayerEntity playerEntity,
                                        BodyStatus status) {

        float keepWarmFactor = computeKeepWarmFactor(playerEntity);
        float passiveShift = getPassiveTemperatureShift(playerEntity, status.temperature);
        // the heat dropping is potential to the diff temp & cooling factor
        // the cooling factor is affect by armor or other condition of the body
        final float stepSize = 0.01F;
        float deltaTemperature = passiveShift * keepWarmFactor * stepSize;

        status.temperature += deltaTemperature;

        Debug.bodyTemp = status.temperature;
    }
}

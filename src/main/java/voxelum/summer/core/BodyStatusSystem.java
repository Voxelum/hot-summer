package voxelum.summer.core;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FurnaceBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Potions;
import net.minecraft.util.Tuple;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import voxelum.summer.Debug;
import voxelum.summer.HotSummerMod;
import voxelum.summer.core.datastruct.BodyStatus;

import java.util.List;

/**
 * The class to compute the body temperature.
 * This is not a real physical model, but a empirical model.
 * So this class also contains some const values to fulfill the computation (which might not correspond to reality)
 */
@Mod.EventBusSubscriber
public class BodyStatusSystem {
    private static int counter = 0;
    @SuppressWarnings("unchecked")
    private static Tuple<Block, Integer>[] BUILT_IN_BLOCK_TEMPS = new Tuple[]{
            new Tuple<>(Blocks.FURNACE, 50),
            new Tuple<>(Blocks.BLAST_FURNACE, 70),
            new Tuple<>(Blocks.TORCH, 30),
            new Tuple<>(Blocks.LAVA, 100),
            new Tuple<>(Blocks.FIRE, 100),
            new Tuple<>(Blocks.ICE, -5),
            new Tuple<>(Blocks.BLUE_ICE, -5),
            new Tuple<>(Blocks.FROSTED_ICE, -5),
            new Tuple<>(Blocks.PACKED_ICE, -5),
    };

    @SuppressWarnings("unchecked")
    private static Tuple<EntityType, Integer>[] BUILT_IN_ENTITY_TEMPS = new Tuple[]{
            new Tuple<>(EntityType.SHEEP, 38),
            new Tuple<>(EntityType.COW, 30),
            new Tuple<>(EntityType.CHICKEN, 20),
            new Tuple<>(EntityType.RABBIT, 20),
            new Tuple<>(EntityType.CAT, 20),
            new Tuple<>(EntityType.WOLF, 30),
            new Tuple<>(EntityType.VILLAGER, 27),
    };

    /**
     * The factor to dismiss the diff between outside temperature and body temperature.
     * Body standard temperature is about 37C but we feel good if the outside is about 27C.
     */
    static final int DISMISS_DIFF_FACTOR = 10;

    /**
     * From environmine, transform mc temperature value to the value more approached to reality
     *
     * @param biomeTemperature The minecraft biome temperature value
     * @return The temperature approach to the reality
     */
    public static float transformBiomeTemperature(float biomeTemperature) {
        // You can calibrate temperatures using these
        // This does not take into account the time of day (These are the midday maximums)
        float maxTemp = 45F; // Desert
        float minTemp = -15F;
        float temp = (float) Math.toRadians(biomeTemperature * 45F);
        return biomeTemperature >= 0 ? MathHelper.sin(temp) * maxTemp : MathHelper.sin(temp) * minTemp;
    }

    /**
     * Get the keep warm factor from player armor.
     *
     * @return The total keep warm factor
     */
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

    public static float getEntityTemperature(LivingEntity entity) {
        for (int i = 0; i < BUILT_IN_ENTITY_TEMPS.length; i++) {
            if (BUILT_IN_ENTITY_TEMPS[i].getA() == entity.getType()) {
                return BUILT_IN_ENTITY_TEMPS[i].getB();
            }
        }
        return getTemperature(entity);
    }

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
                        float distanceFactor = dis == 0 ? 1F : 1F / dis * 0.01F;
                        total += distanceFactor * (temperature - sourceTemperature + DISMISS_DIFF_FACTOR);
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
        List<Entity> others = world.getEntitiesWithinAABBExcludingEntity(origin, origin.getBoundingBox().expand(3, 3, 3).expand(-3, -3, -3));
        for (Entity other : others) {
            if (other instanceof LivingEntity) {
                float temperature = getEntityTemperature((LivingEntity) other);
                if (temperature != Float.MIN_VALUE) {
                    BlockPos src = other.getPosition();
                    BlockPos des = origin.getPosition();
                    int dis = Math.abs(src.getX() - des.getX())
                            + Math.abs(src.getY() - des.getY())
                            + Math.abs(src.getZ() - des.getZ());
                    float factor = dis == 0 ? 1F : 1F / dis;
                    total += factor * (temperature - sourceTemperature + DISMISS_DIFF_FACTOR);
                }
            } else {
                // skip for now
                // later we might add mob temperature
                continue;
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

        // day/night
        long currentTime = world.getDayTime();
        float downfall = biome.getDownfall();
        double shift = Math.sin(Math.PI / 12000D * currentTime);

        float temperatureDayNightConstant = 25;
        biomeTemperature += shift * (1 - downfall) * temperatureDayNightConstant;

        return biomeTemperature - sourceTemperature + DISMISS_DIFF_FACTOR;
    }

    public static float getPassiveTemperatureShift(LivingEntity origin, float sourceTemperature) {
        float biomeTemperature = getTemperatureShiftOnBiome(origin, sourceTemperature);
        float blockTemperature = getTemperatureShiftOnBlocks(origin, sourceTemperature);
        float entitiesTemperature = getTemperatureShiftOnEntities(origin, sourceTemperature);
        float inWaterTemperature = origin.isInWater() ? -5F : 0;

        return biomeTemperature
                + blockTemperature
                + entitiesTemperature
                + inWaterTemperature;
    }

    public static void updateBodyStatus(PlayerEntity playerEntity,
                                        BodyStatus status) {

        float keepWarmFactor = computeKeepWarmFactor(playerEntity);
        // temperature shift is a positive or negative value that computed from environment
        float temperatureShift = getPassiveTemperatureShift(playerEntity, status.temperature);
        status.deltaTemperature = temperatureShift;
        // the heat dropping is potential to the diff temp & cooling factor
        // the cooling factor is affect by armor or other condition of the body
        final float stepSize = 0.005F;
        float deltaTemperature = temperatureShift * keepWarmFactor * stepSize;
        if (Float.isNaN(deltaTemperature)) {
            deltaTemperature = 0;
        }

        // body self balancing function
        if (status.temperature > 36.F) {
            status.hydration -= 0.005F;
            deltaTemperature = deltaTemperature - 0.01F;
        } else if (status.temperature < 0) {
            playerEntity.getFoodStats().addExhaustion(0.01F);
            deltaTemperature = deltaTemperature + 0.01F;
        }

        status.temperature += deltaTemperature;

        Debug.bodyTemp = status.temperature;
    }

    public static void impactToPlayer(PlayerEntity entity, BodyStatus status) {
        if (status.temperature < 34.5F) {
            entity.addPotionEffect(new EffectInstance(HotSummerMod.FREEZING_EFFECT.get(), 2 * 20));
        } else if (status.temperature > 40F) {
            entity.addPotionEffect(new EffectInstance(HotSummerMod.FEVER_EFFECT.get(), 2 * 20));
        }
    }

    @SubscribeEvent
    public static void onWorldTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            counter += 1;
            if (counter == 20) {
                counter = 0;
            }
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        if (counter == 0 || counter == 10) {
            PlayerEntity player = event.player;
            LazyOptional<BodyStatus> capability = player.getCapability(HotSummerMod.CAPABILITY_BODY_STATUS);
            BodyStatus bodyStatus = capability.orElseThrow(Error::new);
            BodyStatusSystem.updateBodyStatus(player, bodyStatus);
        } else if (counter == 20) {
            PlayerEntity player = event.player;
            LazyOptional<BodyStatus> capability = player.getCapability(HotSummerMod.CAPABILITY_BODY_STATUS);
            BodyStatus bodyStatus = capability.orElseThrow(Error::new);
            impactToPlayer(player, bodyStatus);
        }
    }
}

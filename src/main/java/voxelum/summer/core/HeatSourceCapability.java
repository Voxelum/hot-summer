package voxelum.summer.core;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import voxelum.summer.HotSummerMod;
import voxelum.summer.core.datastruct.HeatSource;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static voxelum.summer.HotSummerMod.MODID;

/**
 * UNUSED CLASS
 * Currently, the living entity is statically provide temperature
 */
@Mod.EventBusSubscriber
public class HeatSourceCapability {
    /**
     * The capability location
     */
    public static final ResourceLocation KEY = new ResourceLocation(MODID, "heat_source");

    // register the capability to PlayerEntity
    @SubscribeEvent
    public static void onCapabilityAttached(AttachCapabilitiesEvent<Entity> event) {
//        if (!(event.getObject() instanceof PlayerEntity) && event.getObject() instanceof LivingEntity) {
//            event.addCapability(CAPABILITY_HEAT_SOURCE_LOCATION, new Provider());
//        }
    }

    /**
     * Statically provide the heat source
     */
    static class Provider implements ICapabilityProvider {
        private HeatSource status = new HeatSource();

        Provider(float temperature) {
            status.temperature = temperature;
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return cap == HotSummerMod.CAPABILITY_HEAT_SOURCE
                    ? LazyOptional.of(() -> (T) status)
                    : LazyOptional.empty();
        }
    }
}

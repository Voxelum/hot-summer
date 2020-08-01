package voxelum.summer.bodystatus;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import voxelum.summer.HotSummerMod;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import static voxelum.summer.HotSummerMod.CAPABILITY_BODY_STATUS;
import static voxelum.summer.HotSummerMod.MODID;


/**
 * The class to contain the capability registry and serialization code.
 */
@Mod.EventBusSubscriber
public class BodyStatusCapability {
    /**
     * The capability location
     */
    public static final ResourceLocation CAPAIBLITY_BODY_STATUS_LOCATION = new ResourceLocation(MODID, "body_status");
    public static final Storage STORAGE = new Storage();

    // register the capability to PlayerEntity
    @SubscribeEvent
    public static void onCapabilityAttached(AttachCapabilitiesEvent<Entity> event) {
        if (event.getObject() instanceof PlayerEntity) {
            event.addCapability(CAPAIBLITY_BODY_STATUS_LOCATION, new Provider());
        }
    }

    /**
     * Storage of the body status to serialize the class to NBT
     */
    static class Storage implements Capability.IStorage<BodyStatus> {
        @Nullable
        @Override
        public INBT writeNBT(Capability<BodyStatus> capability, BodyStatus instance, Direction side) {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putFloat("hydration", instance.hydration);
            nbt.putFloat("temperature", instance.temperature);
            return nbt;
        }

        @Override
        public void readNBT(Capability<BodyStatus> capability, BodyStatus instance, Direction side, INBT nbt) {
            CompoundNBT compoundNBT = ((CompoundNBT) nbt);
            instance.hydration = compoundNBT.getFloat("hydration");
            instance.temperature = compoundNBT.getFloat("temperature");
        }
    }

    /**
     * Just a data holder for {@link BodyStatus}. It will only return the {@link BodyStatus} if the capability is {@link HotSummerMod#CAPABILITY_BODY_STATUS}.
     * It has to extends {@link ICapabilitySerializable} to make {@link BodyStatus} to serialize to the nbt.
     * So, it can load for next time the game start.
     */
    static class Provider implements ICapabilitySerializable<CompoundNBT> {
        private BodyStatus status = new BodyStatus();

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
            return cap == HotSummerMod.CAPABILITY_BODY_STATUS ? LazyOptional.of(() -> (T) status) : LazyOptional.empty();
        }

        // use storage to serialize; don't repeat your self

        @Override
        public CompoundNBT serializeNBT() {
            return (CompoundNBT) STORAGE.writeNBT(CAPABILITY_BODY_STATUS, status, null);
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            STORAGE.readNBT(CAPABILITY_BODY_STATUS, status, null, nbt);
        }
    }
}

package voxelum.summer.utils;

import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;

public class CapabilityUtils {
    private CapabilityUtils() {
    }

    @SuppressWarnings("unchecked")
    public static <T> Capability.IStorage<T> emptyStorage() {
        return (Capability.IStorage<T>) EMPTY_STORAGE;
    }

    public static final Capability.IStorage<?> EMPTY_STORAGE = new Capability.IStorage<Object>() {
        @Override
        public INBT writeNBT(Capability<Object> capability, Object instance, Direction side) {
            return null;
        }

        @Override
        public void readNBT(Capability<Object> capability, Object instance, Direction side, INBT nbt) {

        }
    };
}

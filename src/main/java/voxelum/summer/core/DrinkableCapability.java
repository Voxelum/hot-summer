package voxelum.summer.core;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import voxelum.summer.HotSummerMod;
import voxelum.summer.core.datastruct.Drinkable;

@Mod.EventBusSubscriber
public class DrinkableCapability {
    public static final ResourceLocation KEY = new ResourceLocation(HotSummerMod.MODID, "drinkable");

    @SubscribeEvent
    public static void onItemStackAttached(AttachCapabilitiesEvent<ItemStack> event) {
        Item item = event.getObject().getItem();
        if (item == Items.POTION) {
            DrinkableCapability.Provider provider = new DrinkableCapability.Provider();
            provider.value.hydrationRecovery = 0.2F;
            provider.value.deltaTemperature = -0.2F;
            event.addCapability(KEY, provider);
        } else if (item == Items.MILK_BUCKET) {
            DrinkableCapability.Provider provider = new DrinkableCapability.Provider();
            provider.value.hydrationRecovery = 0.2F;
            event.addCapability(KEY, provider);
        } else if (item == HotSummerMod.FRESH_WATER_ITEM.get()) {
            DrinkableCapability.Provider provider = new DrinkableCapability.Provider();
            provider.value.hydrationRecovery = 0.2F;

            provider.value.dirty = false;
            event.addCapability(KEY, provider);
        } else if (item == HotSummerMod.BOTTLE_TEA_ITEM.get()) {
            DrinkableCapability.Provider provider = new DrinkableCapability.Provider();
            provider.value.hydrationRecovery = 0.5F;

            provider.value.dirty = false;
            provider.value.deltaTemperature = 0.1F;
            event.addCapability(KEY, provider);
        }
    }

    static class Provider implements ICapabilitySerializable<CompoundNBT> {
        Drinkable value = new Drinkable();

        public Drinkable getValue() {
            return value;
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            if (cap == HotSummerMod.CAPABILITY_DRINKABLE) {
                return (LazyOptional<T>) LazyOptional.of(this::getValue);
            }
            return LazyOptional.empty();
        }

        @Override
        public CompoundNBT serializeNBT() {
            CompoundNBT nbt = new CompoundNBT();
            nbt.putFloat("deltaTemperature", value.deltaTemperature);
            nbt.putBoolean("dirty", value.dirty);
            nbt.putBoolean("salty", value.salty);
            nbt.putFloat("hydrationRecovery", value.hydrationRecovery);
            return nbt;
        }

        @Override
        public void deserializeNBT(CompoundNBT nbt) {
            value.deltaTemperature = nbt.getFloat("deltaTemperature");
            value.hydrationRecovery = nbt.getFloat("hydrationRecovery");
            value.salty = nbt.getBoolean("salty");
            value.dirty = nbt.getBoolean("dirty");
        }
    }
}

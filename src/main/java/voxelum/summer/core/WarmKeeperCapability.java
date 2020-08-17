package voxelum.summer.core;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import voxelum.summer.HotSummerMod;
import voxelum.summer.core.datastruct.WarmKeeper;

import static voxelum.summer.HotSummerMod.MODID;

@Mod.EventBusSubscriber
public class WarmKeeperCapability {
    public static final ResourceLocation KEY = new ResourceLocation(MODID, "warm_keeper");

    @SuppressWarnings("unchecked")
    private static Tuple<Item, Float>[] BUILT_IN_BLOCK_TEMPS = new Tuple[]{
            new Tuple<>(Items.IRON_HELMET, 0.1F),
            new Tuple<>(Items.IRON_CHESTPLATE, 0.5F),
            new Tuple<>(Items.IRON_LEGGINGS, 0.3F),
            new Tuple<>(Items.IRON_BOOTS, 0.2F),

            new Tuple<>(Items.GOLDEN_HELMET, 0.1F),
            new Tuple<>(Items.GOLDEN_CHESTPLATE, 0.5F),
            new Tuple<>(Items.GOLDEN_LEGGINGS, 0.3F),
            new Tuple<>(Items.GOLDEN_BOOTS, 0.2F),

            new Tuple<>(Items.LEATHER_HELMET, -0.1F),
            new Tuple<>(Items.LEATHER_CHESTPLATE, -0.5F),
            new Tuple<>(Items.LEATHER_LEGGINGS, -0.3F),
            new Tuple<>(Items.LEATHER_BOOTS, -0.2F),

            new Tuple<>(Items.DIAMOND_HELMET, 0F),
            new Tuple<>(Items.DIAMOND_CHESTPLATE, 0F),
            new Tuple<>(Items.DIAMOND_LEGGINGS, 0F),
            new Tuple<>(Items.DIAMOND_BOOTS, 0F),
    };

    @SubscribeEvent
    public static void onAttachItemStack(AttachCapabilitiesEvent<ItemStack> event) {
        for (Tuple<Item, Float> builtInBlockTemp : BUILT_IN_BLOCK_TEMPS) {
            if (event.getObject().getItem() == builtInBlockTemp.getA()) {
                Provider provider = new Provider();
                provider.keeper.keepWarmFactor = builtInBlockTemp.getB();
                event.addCapability(KEY, provider);
            }
        }
    }

    public static class Provider implements ICapabilitySerializable<FloatNBT> {
        WarmKeeper keeper = new WarmKeeper();

        public WarmKeeper getKeeper() {
            return keeper;
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            if (cap == HotSummerMod.CAPABILITY_WARN_KEEPER) {
                return (LazyOptional<T>) LazyOptional.of(this::getKeeper);
            }
            return LazyOptional.empty();
        }

        @Override
        public FloatNBT serializeNBT() {
            return FloatNBT.valueOf(keeper.keepWarmFactor);
        }

        @Override
        public void deserializeNBT(FloatNBT nbt) {
            if (nbt != null) {
                keeper.keepWarmFactor = nbt.getFloat();
            }
        }
    }
}

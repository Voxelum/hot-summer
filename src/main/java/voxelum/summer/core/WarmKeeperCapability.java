package voxelum.summer.core;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.FloatNBT;
import net.minecraft.nbt.IntNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Tuple;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import voxelum.summer.HotSummerMod;
import voxelum.summer.core.datastruct.WarmKeeper;

import javax.annotation.Nonnull;

import static voxelum.summer.HotSummerMod.MODID;

@Mod.EventBusSubscriber
public class WarmKeeperCapability {
    /**
     * The capability location
     */
    public static final ResourceLocation KEY = new ResourceLocation(MODID, "warm_keeper");

    @SuppressWarnings("unchecked")
    private static Tuple<Item, Float>[] BUILT_IN_WARM_KEEPER = new Tuple[]{
            new Tuple<>(Items.IRON_HELMET, 0.1),
            new Tuple<>(Items.IRON_CHESTPLATE, 0.5),
            new Tuple<>(Items.IRON_LEGGINGS, 0.3),
            new Tuple<>(Items.IRON_BOOTS, 0.2),

            new Tuple<>(Items.GOLDEN_HELMET, 0.1),
            new Tuple<>(Items.GOLDEN_CHESTPLATE, 0.5),
            new Tuple<>(Items.GOLDEN_LEGGINGS, 0.3),
            new Tuple<>(Items.GOLDEN_BOOTS, 0.2),

            new Tuple<>(Items.LEATHER_HELMET, -0.1),
            new Tuple<>(Items.LEATHER_CHESTPLATE, -0.5),
            new Tuple<>(Items.LEATHER_LEGGINGS, -0.3),
            new Tuple<>(Items.LEATHER_BOOTS, -0.2),

            new Tuple<>(Items.DIAMOND_HELMET, 0),
            new Tuple<>(Items.DIAMOND_CHESTPLATE, 0),
            new Tuple<>(Items.DIAMOND_LEGGINGS, 0),
            new Tuple<>(Items.DIAMOND_BOOTS, 0),
    };

    @SubscribeEvent
    public static void onCapabilityAttached(AttachCapabilitiesEvent<ItemStack> event) {
        for (int i = 0; i < BUILT_IN_WARM_KEEPER.length; i++) {
            if (BUILT_IN_WARM_KEEPER[i].getA() == event.getObject().getItem()) {
                WarmKeeper warmKeeper = new WarmKeeper();
                warmKeeper.keepWarmFactor = BUILT_IN_WARM_KEEPER[i].getB();
                event.addCapability(KEY, new Provider(warmKeeper));
                return;
            }
        }
    }

    static class Provider implements ICapabilitySerializable<FloatNBT> {
        private final WarmKeeper keeper;

        Provider(WarmKeeper keeper) {
            this.keeper = keeper;
        }

        @Nonnull
        public WarmKeeper getKeeper() {
            return keeper;
        }

        @Override
        public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
            if (cap == HotSummerMod.CAPABILITY_WARN_KEEPER) {
                return (LazyOptional<T>) LazyOptional.of(this::getKeeper);
            }
            return null;
        }

        @Override
        public FloatNBT serializeNBT() {
            return FloatNBT.valueOf(keeper.keepWarmFactor);
        }

        @Override
        public void deserializeNBT(FloatNBT nbt) {
            keeper.keepWarmFactor = nbt.getFloat();
        }
    }
}

package voxelum.summer.core;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import voxelum.summer.HotSummerMod;
import voxelum.summer.core.datastruct.BodyStatus;
import voxelum.summer.core.datastruct.Drinkable;
import voxelum.summer.utils.DrinkableHelper;

@Mod.EventBusSubscriber
public class DrinkableCapability {
    public static final ResourceLocation KEY = new ResourceLocation(HotSummerMod.MODID, "drinkable");

    @SubscribeEvent
    public static void onItemUsed(LivingEntityUseItemEvent.Stop event) {
        if (event.getEntity().world.isRemote) {
            return;
        }
        if (event.getEntity() instanceof PlayerEntity) {
            LazyOptional<Drinkable> optional = event.getItem().getCapability(HotSummerMod.CAPABILITY_DRINKABLE);
            if (optional.isPresent()) {
                Drinkable drinkable = optional.orElseThrow(Error::new);
                BodyStatus bodyStatus = event.getEntity().getCapability(HotSummerMod.CAPABILITY_BODY_STATUS).orElseThrow(Error::new);

                bodyStatus.incrementHydration(drinkable.hydrationRecovery);
                bodyStatus.temperature += drinkable.deltaTemperature;

                if (drinkable.dirty) {
                    World world = event.getEntity().getEntityWorld();
                    int i = world.rand.nextInt(6);
                    if (i == 0) {
                        ((PlayerEntity) event.getEntity()).addPotionEffect(new EffectInstance(Effects.NAUSEA, 10 * 2000));
                    } else if (i == 1) {
                        ((PlayerEntity) event.getEntity()).addPotionEffect(new EffectInstance(Effects.WEAKNESS, 10 * 2000));
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onItemStackAttached(AttachCapabilitiesEvent<ItemStack> event) {
        Item item = event.getObject().getItem();
        if (item == Items.POTION) {
            Provider provider = new Provider();
            provider.value.hydrationRecovery = 0.2F;
            provider.value.deltaTemperature = -0.2F;
            event.addCapability(KEY, provider);
        } else if (item == Items.MILK_BUCKET) {
            Provider provider = new Provider();
            provider.value.hydrationRecovery = 0.2F;
            event.addCapability(KEY, provider);
        } else if (item == HotSummerMod.ICE_WATER_ITEM.get()) {
            Provider provider = new Provider();
            provider.value.dirty = false;
            provider.value.hydrationRecovery = 0.2F;
            provider.value.deltaTemperature = -0.9F;
            event.addCapability(KEY, provider);
        } else if (item == HotSummerMod.HOT_WATER_ITEM.get()) {
            Provider provider = new Provider();
            provider.value.dirty = false;
            provider.value.hydrationRecovery = 0.2F;
            provider.value.deltaTemperature = 0.2F;
            event.addCapability(KEY, provider);
        } else if (item == HotSummerMod.SALT_WATER_ITEM.get()) {
            Provider provider = new Provider();
            provider.value.dirty = false;
            provider.value.salty = true;
            provider.value.hydrationRecovery = 0.2F;
            provider.value.deltaTemperature = 0;
            event.addCapability(KEY, provider);
        } else if (item == HotSummerMod.BOTTLE_TEA_ITEM.get()) {
            Provider provider = new Provider();
            provider.value.dirty = false;
            provider.value.hydrationRecovery = 0.5F;
            provider.value.deltaTemperature = 0.1F;
            event.addCapability(KEY, provider);
        }
    }

    private static Drinkable matchIceWater(IInventory inventory) {
        int size = inventory.getSizeInventory();
        int snowball = 0;
        Drinkable drinkable = null;
        for (int i = 0; i < size; i++) {
            ItemStack stackInSlot = inventory.getStackInSlot(i);
            Item item = stackInSlot.getItem();
            if (item == Items.SNOW_BLOCK) {
                snowball += 1;
            } else {
                LazyOptional<Drinkable> capability = stackInSlot.getCapability(HotSummerMod.CAPABILITY_DRINKABLE);
                if (capability.isPresent()) {
                    drinkable = capability.orElseThrow(Error::new);
                }
            }
        }
        if (snowball > 0) {
            return drinkable;
        }
        return null;
    }

    @SubscribeEvent
    public static void onItemStackRecipe(PlayerEvent.ItemCraftedEvent event) {
        ItemStack itemStack = event.getCrafting();
        itemStack.getCapability(HotSummerMod.CAPABILITY_DRINKABLE).ifPresent((drinkable) -> {
            Drinkable prevDrink = matchIceWater(event.getInventory());
            if (prevDrink != null) {
                drinkable.hydrationRecovery = prevDrink.hydrationRecovery;
                drinkable.dirty = prevDrink.dirty;
                drinkable.salty = prevDrink.salty;
                drinkable.deltaTemperature = prevDrink.deltaTemperature - 0.5F;
            }
        });
    }

    @SubscribeEvent
    public static void onSmelt(PlayerEvent.ItemSmeltedEvent event) {
        ItemStack itemStack = event.getSmelting();
        itemStack.getCapability(HotSummerMod.CAPABILITY_DRINKABLE).ifPresent((drinkable) -> {
            drinkable.deltaTemperature = 0.5F;
            drinkable.dirty = false;
            drinkable.salty = false;
        });
    }

    @SubscribeEvent
    public static void onTooltip(ItemTooltipEvent event) {
        LazyOptional<Drinkable> capability = event.getItemStack().getCapability(HotSummerMod.CAPABILITY_DRINKABLE);
        if (capability.isPresent()) {
            Drinkable drinkable = capability.orElseThrow(Error::new);
            event.getToolTip().add(DrinkableHelper.getTemperatureText(drinkable));
            event.getToolTip().add(DrinkableHelper.getDirtyText(drinkable));
        }
    }

//    @SubscribeEvent
//    public static void onPlayerInteractBlock(PlayerInteractEvent.RightClickEmpty event) {
//        Entity entity = event.getEntity();
//
//        RayTraceResult rayTraceResult = DrinkableItem.rayHelper(entity.world, (PlayerEntity) entity, RayTraceContext.FluidMode.SOURCE_ONLY);
//        if (rayTraceResult.getType() == RayTraceResult.Type.BLOCK) {
//            Vec3d hitVec = rayTraceResult.getHitVec();
//            BlockPos pos = new BlockPos(hitVec);
//            Biome biome = entity.world.getBiome(pos);
//
//        }
//    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickItem event) {
        // this has to be on both client/server side
        // this cannot use PlayerInteractEvent.RightClickBlock since the PlayerInteractEvent.RightClickBlock
        // do not raytrace the liquid
        World world = event.getWorld();
        ItemStack itemStack = event.getItemStack();
        if (itemStack.getItem() == Items.GLASS_BOTTLE && event.getEntity() instanceof PlayerEntity) {
            // replace normal glass bottle behavior
            boolean success = DrinkableHelper.onGlassBottleItemUse(world, (PlayerEntity) event.getEntity(), itemStack);
            if (success) {
                event.setCanceled(true);
            }
        }
    }

    static class Provider implements ICapabilityProvider {
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
    }
}

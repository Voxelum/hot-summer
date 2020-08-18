package voxelum.summer.core;

import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.PacketDistributor;
import voxelum.summer.HotSummerMod;
import voxelum.summer.core.datastruct.BodyStatus;
import voxelum.summer.core.datastruct.Drinkable;
import voxelum.summer.core.message.DrinkWaterMessage;
import voxelum.summer.items.DrinkableItem;
import voxelum.summer.utils.DrinkableHelper;

@Mod.EventBusSubscriber
public class DrinkableSystem {
    /**
     * Core drink logic
     */
    public static void drink(PlayerEntity entity, BodyStatus bodyStatus, Drinkable drinkable) {
        bodyStatus.incrementHydration(drinkable.hydrationRecovery);
        bodyStatus.temperature += drinkable.deltaTemperature;

        if (drinkable.dirty) {
            World world = entity.getEntityWorld();
            int i = world.rand.nextInt(6);
            if (i == 0) {
                entity.addPotionEffect(new EffectInstance(Effects.NAUSEA, 20 * 10));
            } else if (i == 1) {
                entity.addPotionEffect(new EffectInstance(Effects.WEAKNESS, 20 * 10));
            }
        }
        if (drinkable.salty) {
            World world = entity.getEntityWorld();
            int i = world.rand.nextInt(2);
            if (i == 0) {
                entity.addPotionEffect(new EffectInstance(HotSummerMod.DEHYDRATION_EFFECT.get(), 2000));
            }
        }
    }

    /**
     * Decorate the drinkable according to the position in the world.
     * Different biome might create different effect.
     */
    public static void decorateDrinkable(Drinkable drinkable, World world, BlockPos pos) {
        Biome biome = world.getBiome(pos);
        if (biome == Biomes.OCEAN
                || biome == Biomes.COLD_OCEAN
                || biome == Biomes.DEEP_WARM_OCEAN
                || biome == Biomes.DEEP_LUKEWARM_OCEAN
                || biome == Biomes.DEEP_COLD_OCEAN
                || biome == Biomes.DEEP_FROZEN_OCEAN
                || biome == Biomes.WARM_OCEAN
                || biome == Biomes.DEEP_OCEAN
                || biome == Biomes.FROZEN_OCEAN
                || biome == Biomes.LUKEWARM_OCEAN) {
            drinkable.salty = true;
        }

        if (pos.getY() > 50 || world.rand.nextInt(3) == 0) {
            drinkable.dirty = false;
        } else {
            drinkable.dirty = true;
        }
        drinkable.hydrationRecovery = 0.2F;
    }

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
                drink((PlayerEntity) event.getEntity(), bodyStatus, drinkable);
            }
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

    @SubscribeEvent
    public static void onPlayerInteractBlock(PlayerInteractEvent.RightClickBlock event) {
        if (event.getWorld().isRemote) {
            return;
        }
        if (event.getItemStack() != ItemStack.EMPTY) {
            return;
        }
        if (event.getHand() != Hand.MAIN_HAND) {
            return;
        }

        PlayerEntity entity = event.getPlayer();
        RayTraceResult rayTraceResult = DrinkableItem.rayHelper(entity.world, entity, RayTraceContext.FluidMode.SOURCE_ONLY);
        if (rayTraceResult.getType() == RayTraceResult.Type.BLOCK) {
            Vec3d hitVec = rayTraceResult.getHitVec();
            BlockPos pos = new BlockPos(hitVec);
            if (entity.world.getFluidState(pos).isTagged(FluidTags.WATER)) {
                entity.world.playSound(null, pos, SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.BLOCKS, 2F, 1F);
                Drinkable drinkable = new Drinkable();
                decorateDrinkable(drinkable, event.getWorld(), pos);
                drink(event.getPlayer(), event.getPlayer().getCapability(HotSummerMod.CAPABILITY_BODY_STATUS).orElseThrow(Error::new), drinkable);
                event.setCanceled(true);
                event.setCancellationResult(ActionResultType.CONSUME);
            }
        }
    }

    @SubscribeEvent
    public static void onPlayerInteractEmpty(PlayerInteractEvent.RightClickEmpty event) {
        // TODO: send drink water
        Entity entity = event.getEntity();

        RayTraceResult rayTraceResult = DrinkableItem.rayHelper(entity.world, (PlayerEntity) entity, RayTraceContext.FluidMode.SOURCE_ONLY);
        if (rayTraceResult.getType() == RayTraceResult.Type.BLOCK) {
            Vec3d hitVec = rayTraceResult.getHitVec();
            BlockPos pos = new BlockPos(hitVec);
            HotSummerMod.NETWORK.sendToServer(DrinkWaterMessage.of(pos));
        }
    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickItem event) {
        // this cannot use PlayerInteractEvent.RightClickBlock since the PlayerInteractEvent.RightClickBlock
        // do not raytrace the liquid
        World world = event.getWorld();
        ItemStack itemStack = event.getItemStack();
        if (world.isRemote) {
            return;
        }
        PlayerEntity player = event.getPlayer();
        boolean check = itemStack.getItem() == Items.GLASS_BOTTLE
                || itemStack == ItemStack.EMPTY;

        if (check) {
            // replace normal glass bottle behavior
            RayTraceResult raytraceresult = DrinkableItem.rayHelper(world, player, RayTraceContext.FluidMode.SOURCE_ONLY);
            if (raytraceresult.getType() == RayTraceResult.Type.BLOCK) {
                BlockPos pos = ((BlockRayTraceResult) raytraceresult).getPos();
                if (!world.isBlockModifiable(player, pos)) {
                    return;
                }

                if (world.getFluidState(pos).isTagged(FluidTags.WATER)) {
                    world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);

                    if (itemStack.getItem() != Items.GLASS_BOTTLE) {
                        ItemStack potion = new ItemStack(Items.POTION);
                        Drinkable drinkable = potion.getCapability(HotSummerMod.CAPABILITY_DRINKABLE).orElseThrow(Error::new);
                        decorateDrinkable(drinkable, world, pos);
                        DrinkableHelper.turnBottleIntoItem(itemStack, player, PotionUtils.addPotionToItemStack(potion, Potions.WATER));
                    } else {
                        Drinkable drinkable = new Drinkable();
                        decorateDrinkable(drinkable, world, pos);
                        drink(player, player.getCapability(HotSummerMod.CAPABILITY_BODY_STATUS).orElseThrow(Error::new), drinkable);
                    }
                    event.setCanceled(true);
                }
            }
        }
    }
}

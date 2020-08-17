package voxelum.summer.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.stats.Stats;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.ActionResult;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.Biomes;
import voxelum.summer.HotSummerMod;
import voxelum.summer.core.DrinkableCapability;
import voxelum.summer.core.datastruct.Drinkable;
import voxelum.summer.items.DrinkableItem;

public class DrinkableHelper {
    public static void updateItemDisplayName(ItemStack itemStack, Drinkable drinkable) {
        CompoundNBT display = itemStack.getOrCreateChildTag("display");
        ITextComponent displayName = itemStack.getItem().getDisplayName(itemStack);
        TranslationTextComponent temperatureText = getTemperatureText(drinkable);
        if (!temperatureText.getKey().equals("item.drinkable.normal")) {
            displayName.appendSibling(temperatureText);
        }
        displayName.appendSibling(getDirtyText(drinkable));
        display.putString("Name", ITextComponent.Serializer.toJson(displayName));
    }

    public static TranslationTextComponent getTemperatureText(Drinkable drinkable) {
        TranslationTextComponent component;
        if (drinkable.deltaTemperature > 0) {
            component = new TranslationTextComponent("item.drinkable.hot");
        } else if (drinkable.deltaTemperature < 0) {
            component = new TranslationTextComponent("item.drinkable.cold");
        } else {
            component = new TranslationTextComponent("item.drinkable.normal");
        }
        component.applyTextStyle(TextFormatting.GRAY);
        return component;
    }

    public static ITextComponent getDirtyText(Drinkable drinkable) {
        ITextComponent component;
        if (drinkable.dirty) {
            component = new TranslationTextComponent("item.drinkable.dirty");
        } else {
            component = new TranslationTextComponent("item.drinkable.clean");
        }
        component.applyTextStyle(TextFormatting.GRAY);
        return component;
    }

    /**
     * Replace {@link net.minecraft.item.GlassBottleItem#onItemRightClick}
     */
    public static boolean onGlassBottleItemUse(World world, PlayerEntity player, ItemStack itemStack) {
        RayTraceResult raytraceresult = DrinkableItem.rayHelper(world, player, RayTraceContext.FluidMode.SOURCE_ONLY);
        if (raytraceresult.getType() == RayTraceResult.Type.BLOCK) {
            BlockPos blockpos = ((BlockRayTraceResult) raytraceresult).getPos();
            if (!world.isBlockModifiable(player, blockpos)) {
                return false;
            }

            if (world.getFluidState(blockpos).isTagged(FluidTags.WATER)) {
                world.playSound(player, player.getPosX(), player.getPosY(), player.getPosZ(), SoundEvents.ITEM_BOTTLE_FILL, SoundCategory.NEUTRAL, 1.0F, 1.0F);

                ItemStack potion = new ItemStack(Items.POTION);
                Drinkable drinkable = potion.getCapability(HotSummerMod.CAPABILITY_DRINKABLE).orElseThrow(Error::new);

                Biome biome = world.getBiome(blockpos);
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

                if (blockpos.getY() > 50 || world.rand.nextInt(3) == 0) {
                    drinkable.dirty = false;
                } else {
                    drinkable.dirty = true;
                }

                drinkable.hydrationRecovery = 0.2F;

                turnBottleIntoItem(itemStack, player, PotionUtils.addPotionToItemStack(potion, Potions.WATER));
                return true;
            }
        }
        return false;
    }

    private static ItemStack turnBottleIntoItem(ItemStack originalItem, PlayerEntity player, ItemStack water) {
        originalItem.shrink(1);
        player.addStat(Stats.ITEM_USED.get(Items.GLASS_BOTTLE));
        if (originalItem.isEmpty()) {
            return water;
        } else {
            if (!player.inventory.addItemStackToInventory(water)) {
                player.dropItem(water, false);
            }

            return originalItem;
        }
    }
}

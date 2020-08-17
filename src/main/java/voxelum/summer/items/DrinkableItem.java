package voxelum.summer.items;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.*;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.PotionUtils;
import net.minecraft.stats.Stats;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

public class DrinkableItem extends Item {
    private final Item drinkContainerItem;

    public static RayTraceResult rayHelper(World worldIn, PlayerEntity player, RayTraceContext.FluidMode fluidMode) {
        return Item.rayTrace(worldIn, player, fluidMode);
    }

    public DrinkableItem(Item.Properties builder, Item containerItem) {
        super(builder);
        this.drinkContainerItem = containerItem;
    }

    public Item getDrinkContainerItem() {
        return drinkContainerItem;
    }

    //    @OnlyIn(Dist.CLIENT)
//    public ItemStack getDefaultInstance() {
//        return PotionUtils.addPotionToItemStack(super.getDefaultInstance(), Potions.WATER);
//    }

    /**
     * Called when the player finishes using this Item (E.g. finishes eating.). Not called when the player stops using
     * the Item before the action is complete.
     */
    public ItemStack onItemUseFinish(ItemStack stack, World worldIn, LivingEntity entityLiving) {
        PlayerEntity playerentity = entityLiving instanceof PlayerEntity ? (PlayerEntity) entityLiving : null;
        if (playerentity instanceof ServerPlayerEntity) {
            CriteriaTriggers.CONSUME_ITEM.trigger((ServerPlayerEntity) playerentity, stack);
        }

        if (!worldIn.isRemote) {
            for (EffectInstance effectinstance : PotionUtils.getEffectsFromStack(stack)) {
                if (effectinstance.getPotion().isInstant()) {
                    effectinstance.getPotion().affectEntity(playerentity, playerentity, entityLiving, effectinstance.getAmplifier(), 1.0D);
                } else {
                    entityLiving.addPotionEffect(new EffectInstance(effectinstance));
                }
            }
        }

        if (playerentity != null) {
            playerentity.addStat(Stats.ITEM_USED.get(this));
            if (!playerentity.abilities.isCreativeMode) {
                stack.shrink(1);
            }
        }

        if (playerentity == null || !playerentity.abilities.isCreativeMode) {
            if (stack.isEmpty()) {
                return new ItemStack(drinkContainerItem);
            }

            if (playerentity != null) {
                playerentity.inventory.addItemStackToInventory(new ItemStack(drinkContainerItem));
            }
        }

        return stack;
    }

    /**
     * How long it takes to use or consume an item
     */
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    /**
     * returns the action that specifies what animation to play when the items is being used
     */
    public UseAction getUseAction(ItemStack stack) {
        return UseAction.DRINK;
    }

    /**
     * Called to trigger the item's "innate" right click behavior. To handle when this item is used on a Block, see
     * {@link #onItemUse}.
     */
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        playerIn.setActiveHand(handIn);
        return ActionResult.resultSuccess(playerIn.getHeldItem(handIn));
    }

    /**
     * allows items to add custom lines of information to the mouseover description
     */
    @OnlyIn(Dist.CLIENT)
    public void addInformation(ItemStack stack, @Nullable World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
    }

//    /**
//     * Returns true if this item has an enchantment glint. By default, this returns <code>stack.isItemEnchanted()</code>,
//     * but other items can override it (for instance, written books always return true).
//     * <p>
//     * Note that if you override this method, you generally want to also call the super version (on {@link Item}) to get
//     * the glint for enchanted items. Of course, that is unnecessary if the overwritten version always returns true.
//     */
//    public boolean hasEffect(ItemStack stack) {
//        return super.hasEffect(stack) || !PotionUtils.getEffectsFromStack(stack).isEmpty();
//    }

//    /**
//     * returns a list of items with the same ID, but different meta (eg: dye returns 16 items)
//     */
//    public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
//        if (this.isInGroup(group)) {
//            for (Potion potion : Registry.POTION) {
//                if (potion != Potions.EMPTY) {
//                    items.add(PotionUtils.addPotionToItemStack(new ItemStack(this), potion));
//                }
//            }
//        }
//    }
}

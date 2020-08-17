package voxelum.summer.core.datastruct;

import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ArmorMaterial;
import net.minecraft.item.ItemStack;

/**
 * The data structure hold for a armor to keep warm for player
 * This should hook to a {@link net.minecraft.item.ItemStack}
 */
public class WarmKeeper {
    /**
     * The factor to multiply to the delta temperate.
     * - =0 for not affect the temperature apply
     * - <0 for make temperature harder to impact the body temperature, like
     * - >0 to make the cloth can conduct the temperature faster, like iron armor
     */
    public float keepWarmFactor;
}

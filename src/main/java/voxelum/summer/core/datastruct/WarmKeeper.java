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
    private float getKeepWarmFactor(ItemStack stack,PlayerEntity player){
        if(player.getItemStackFromSlot(EquipmentSlotType.fromSlotTypeAndIndex(EquipmentSlotType.Group.ARMOR,3)).equals(ArmorMaterial.DIAMOND)){
            return 0.1f;
        }
        if(player.getItemStackFromSlot(EquipmentSlotType.fromSlotTypeAndIndex(EquipmentSlotType.Group.ARMOR,3)).equals(ArmorMaterial.GOLD)){
            return 0.3f;
        }
        if(player.getItemStackFromSlot(EquipmentSlotType.fromSlotTypeAndIndex(EquipmentSlotType.Group.ARMOR,3)).equals(ArmorMaterial.IRON)){
            return 0.5f;
        }
        if(player.getItemStackFromSlot(EquipmentSlotType.fromSlotTypeAndIndex(EquipmentSlotType.Group.ARMOR,3)).equals(ArmorMaterial.CHAIN)){
            return 0.7f;
        }
        if(player.getItemStackFromSlot(EquipmentSlotType.fromSlotTypeAndIndex(EquipmentSlotType.Group.ARMOR,3)).equals(ArmorMaterial.LEATHER)){
            return 0.9f;
        }
        else return 1;
    }
    /**
     * The factor to multiply to the delta temperate.
     * - =0 for not affect the temperature apply
     * - <0 for make temperature harder to impact the body temperature, like
     * - >0 to make the cloth can conduct the temperature faster, like iron armor
     */
    public float keepWarmFactor;
}

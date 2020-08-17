package voxelum.summer.core;

import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import voxelum.summer.HotSummerMod;
import voxelum.summer.core.datastruct.BodyStatus;

import javax.xml.bind.annotation.XmlType;
@Mod.EventBusSubscriber
public class DrinkWater {
    @SubscribeEvent
    public static void onRightClickWater(PlayerInteractEvent.RightClickBlock event, World world, PlayerEntity player){
        if(event.getUseBlock().equals(Blocks.WATER)){
            world.playSound(player,player.getPosition(),SoundEvents.ENTITY_GENERIC_DRINK,SoundCategory.PLAYERS,1,1);
            player.getCapability(HotSummerMod.CAPABILITY_BODY_STATUS).ifPresent((bb)->{
                bb.hydration+=0.1;
            });

        }
    }
}

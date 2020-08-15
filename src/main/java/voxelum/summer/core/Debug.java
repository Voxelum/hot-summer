package voxelum.summer.core;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import voxelum.summer.HotSummerMod;

@Mod.EventBusSubscriber
public class Debug {
    public static float biomeTemp;
    public static float entityTemp;
    public static float blockTemp;
    public static float bodyTemp;

    private static int counter = 0;

    @SubscribeEvent
    public static void onWorldTick(TickEvent.ServerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            counter += 1;
            if (counter == 10) {
                counter = 0;
            }
        }
    }

    @SubscribeEvent
    public static void onTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || counter != 1) {
            return;
        }
        PlayerEntity player = event.player;
        LazyOptional<BodyStatus> capability = player.getCapability(HotSummerMod.CAPABILITY_BODY_STATUS);
        BodyStatus bodyStatus = capability.orElseThrow(Error::new);
        BodyAlgorithm.updateBodyStatus(player, bodyStatus);
    }

    @SubscribeEvent
    public static void onRender(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            Minecraft.getInstance().fontRenderer.drawString(String.format("%.2f", bodyTemp), 0, 0, 0);
            Minecraft.getInstance().fontRenderer.drawString(String.format("%.2f", entityTemp) + "", 0, 10, 0);
            Minecraft.getInstance().fontRenderer.drawString(String.format("%.2f", blockTemp), 0, 20, 0);
            Minecraft.getInstance().fontRenderer.drawString(String.format("%.2f", biomeTemp), 0, 30, 0);
        }
    }

    @SubscribeEvent
    public static void onEntityJoinWorld(EntityJoinWorldEvent event) {
        ClientPlayerEntity player = Minecraft.getInstance().player;
        if (event.getEntity() == player) {
            BlockPos position = player.getPosition();
            event.getWorld().getChunk(position);
        }
    }
}

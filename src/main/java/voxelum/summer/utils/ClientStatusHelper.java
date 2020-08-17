package voxelum.summer.utils;

import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import voxelum.summer.HotSummerMod;
import voxelum.summer.core.datastruct.BodyStatus;

public class ClientStatusHelper {
    @OnlyIn(Dist.CLIENT)
    public static void updateClientStatus(BodyStatus s) {
        if (Minecraft.getInstance().player != null) {
            Minecraft.getInstance().player.getCapability(HotSummerMod.CAPABILITY_BODY_STATUS).ifPresent((status) -> {
                status.deltaTemperature = s.deltaTemperature;
                status.hydration = s.hydration;
                status.temperature = s.temperature;
            });
        }
    }
}

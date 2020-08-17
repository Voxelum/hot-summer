package voxelum.summer.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import voxelum.summer.HotSummerMod;
import voxelum.summer.core.datastruct.BodyStatus;

@Mod.EventBusSubscriber
@OnlyIn(Dist.CLIENT)
public class GuiBodyStatus {
    public static final ResourceLocation GUI_LOCATION = new ResourceLocation("hotsummer", "textures/gui/gui.png");

    private static BodyStatus status = null;

    private static float getEnvironTemperaturePercentage() {
        float current = status.deltaTemperature + 10;
        if (current < 0) {
            return 0;
        }
        if (current > 20) {
            return 1;
        }
        return (current) / 20;
    }

    private static float getTemperaturePercentage() {
        float max = 42F;
        float min = 32F;
        float current = status.temperature;
        if (current < min) {
            return 0;
        }
        if (current > max) {
            return 1;
        }
        return (current - min) / (max - min);
    }

    private static float getHydrationPercentage() {
        float current = status.hydration;
        return current / 1F;
    }

    @SubscribeEvent
    public static void onPlayerLogin(ClientPlayerNetworkEvent.LoggedInEvent event) {
        status = event.getPlayer().getCapability(HotSummerMod.CAPABILITY_BODY_STATUS).orElseThrow(Error::new);
    }

    @SubscribeEvent
    public static void onPlayerRespawn(ClientPlayerNetworkEvent.RespawnEvent event) {
        status = event.getPlayer().getCapability(HotSummerMod.CAPABILITY_BODY_STATUS).orElseThrow(Error::new);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGameOverlayEvent.Post event) {
        if (status == null) return;
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            float temperature = getTemperaturePercentage();
            float hydration = getHydrationPercentage();
            float envTemperature = getEnvironTemperaturePercentage();

            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            Minecraft.getInstance().getTextureManager().bindTexture(GUI_LOCATION);

            // base
            GuiHelper.drawSprite(5, 205, 0, 0, 0, 76, 9, 128, 128);
            // water blue
            GuiHelper.drawSprite(5, 205, 0, 0, 9, (int) (76 * hydration), 9, 128, 128);
            // water icon
            GuiHelper.drawSprite(1, 202, 0, 15, 38, 14, 16, 128, 128);

            // temperature
            GuiHelper.drawSprite(2, 220, 0, 0, 18, 80, 18, 128, 128);

            int maxWidth = 72;

            // body temperature pivot
            GuiHelper.drawSprite(4 + (int) (maxWidth * temperature), 225, 0, 2, 37, 3, 10, 128, 128);

            // env temperature pivot
            GuiHelper.drawSprite(4 + (int) (maxWidth * envTemperature), 225, 0, 2 + 7, 37, 4, 10, 128, 128);

            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GuiHelper.drawString(String.format("%.2f", status.hydration * 100) + " %", 90, 205, 0xFFFFFFFF);
            GuiHelper.drawString(String.format("%.2f C", status.temperature), 90, 220, 0xFFFFFFFF);
        }
    }
}

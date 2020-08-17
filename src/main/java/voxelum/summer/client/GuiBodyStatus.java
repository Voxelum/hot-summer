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
        float current = status.deltaTemperature + 15;
        if (current < 0) {
            return 0;
        }
        if (current > 10) {
            return 1;
        }
        return (current) / 10;
    }

    private static float getTemperaturePercentage() {
        int max = 45;
        int min = 33;
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
//            drawSprite(5, 225, 0, 0, 17, 55, 9, 128, 128);

            // temperature
            GuiHelper.drawSprite(2, 220, 0, 0, 18, 80, 18, 128, 128);

            int maxWidth = 72;

            // body temperature pivot
            GuiHelper.drawSprite(4 + (int) (maxWidth * temperature), 225, 0, 2, 37, 3, 10, 128, 128);

            // env temperature pivot
            GuiHelper.drawSprite(4 + (int) (maxWidth * envTemperature), 225, 0, 2 + 7, 37, 4, 10, 128, 128);

//            GuiHelper.drawString(status.deltaTemperature);
        }
    }
}

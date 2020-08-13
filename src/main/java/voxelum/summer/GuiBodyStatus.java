package voxelum.summer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class GuiBodyStatus {
    public static final ResourceLocation THERMOMETER_LOCATION = new ResourceLocation("hotsummer", "textures/gui/thermometer-flat.png");

    static class Gui extends AbstractGui {
        @Override
        public void fillGradient(int x, int y, int x2, int y2, int start, int end) {
            super.fillGradient(x, y, x2, y2, start, end);
        }
    }

    public static final Gui GUI = new Gui();

    public static void drawSprite(int x, int y, int offset, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        AbstractGui.blit(x, y, offset, u, v, width, height, textureWidth, textureHeight);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            int startColor = 0xFFFF0000;
            int middleColor = 0xFF00FF00;
            int endColor = 0xFF0000FF;

            GUI.fillGradient(400 + 13, 205 + 1, 400 + 19, 205 + 31 - 15, startColor, middleColor);
            GUI.fillGradient(400 + 13, 205 + 1 + 15, 400 + 19, 205 + 31, middleColor, endColor);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            Minecraft.getInstance().getTextureManager().bindTexture(THERMOMETER_LOCATION);
            drawSprite(400, 205, 0, 0, 0, 32, 32, 32, 32);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}

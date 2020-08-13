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
//    public static final ResourceLocation THERMOMETER_LOCATION = new ResourceLocation("hotsummer", "textures/gui/thermometer.png");
//
//    public static void drawSprite(int x, int y, int offset, float u, float v, int width, int height, int textureWidth, int textureHeight) {
//        AbstractGui.blit(x, y, offset, u, v, width, height, textureWidth, textureHeight);
//    }
//
//    @SubscribeEvent
//    public static void onRenderGui(RenderGameOverlayEvent.Post event) {
//        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
//            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
//            Minecraft.getInstance().getTextureManager().bindTexture(THERMOMETER_LOCATION);
//            drawSprite(0, 0, 0, 0, 0, 16, 16, 16, 16);
//        }
//    }
}

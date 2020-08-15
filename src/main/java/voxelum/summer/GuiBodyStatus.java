package voxelum.summer;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import voxelum.summer.core.Debug;

@Mod.EventBusSubscriber
public class GuiBodyStatus {
    public static final ResourceLocation THERMOMETER_LOCATION = new ResourceLocation("hotsummer", "textures/gui/thermometer-flat.png");

    static class Gui extends AbstractGui {
        @Override
        public void fillGradient(int x, int y, int x2, int y2, int start, int end) {
            super.fillGradient(x, y, x2, y2, start, end);
        }

    }

    public static void fill(int x, int y, int x2, int y2, int argb) {
        AbstractGui.fill(x, y, x2, y2, argb);
    }

    public static final Gui GUI = new Gui();

    public static void drawSprite(int x, int y, int offset, float u, float v, int width, int height, int textureWidth, int textureHeight) {
        AbstractGui.blit(x, y, offset, u, v, width, height, textureWidth, textureHeight);
    }

    public static void fillMixture(Matrix4f matrix, int x1, int y1, int x2, int y2, int color1, int color2, float percentage) {
        if (x1 < x2) {
            int i = x1;
            x1 = x2;
            x2 = i;
        }

        if (y1 < y2) {
            int j = y1;
            y1 = y2;
            y2 = j;
        }

        float a1 = (float) (color1 >> 24 & 255) / 255.0F;
        float r1 = (float) (color1 >> 16 & 255) / 255.0F;
        float g1 = (float) (color1 >> 8 & 255) / 255.0F;
        float b1 = (float) (color1 & 255) / 255.0F;


        float a2 = (float) (color2 >> 24 & 255) / 255.0F;
        float r2 = (float) (color2 >> 16 & 255) / 255.0F;
        float g2 = (float) (color2 >> 8 & 255) / 255.0F;
        float b2 = (float) (color2 & 255) / 255.0F;

        float a = a1 * (1 - percentage) + a2 * (percentage);
        float r = r1 * (1 - percentage) + r2 * (percentage);
        float g = g1 * (1 - percentage) + g2 * (percentage);
        float b = b1 * (1 - percentage) + b2 * (percentage);

        BufferBuilder bufferbuilder = Tessellator.getInstance().getBuffer();
        RenderSystem.enableBlend();
        RenderSystem.disableTexture();
        RenderSystem.defaultBlendFunc();
        bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
        bufferbuilder.pos(matrix, (float) x1, (float) y2, 0.0F).color(r, g, b, a).endVertex();
        bufferbuilder.pos(matrix, (float) x2, (float) y2, 0.0F).color(r, g, b, a).endVertex();
        bufferbuilder.pos(matrix, (float) x2, (float) y1, 0.0F).color(r, g, b, a).endVertex();
        bufferbuilder.pos(matrix, (float) x1, (float) y1, 0.0F).color(r, g, b, a).endVertex();
        bufferbuilder.finishDrawing();
        WorldVertexBufferUploader.draw(bufferbuilder);
        RenderSystem.enableTexture();
        RenderSystem.disableBlend();
    }

    private static float getPercentage() {
        int max = 41;
        int min = 35;
        float current = Debug.bodyTemp;
        return Math.max(0.001F, current - min) / (max - min);
    }

    @SubscribeEvent
    public static void onRenderGui(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            int x1 = 400 + 13;
            int y1 = 205 + 1;
            int x2 = 400 + 19;
            int y2 = 205 + 31;

            int color1;
            int color2;
            float percentage = getPercentage();

            {
                int red = 0xFFff3535; // 0
                int yellow = 0xFFe6f663; // 0.25
                int green = 0xFF5bed62; // 0.5
                int lightBlue = 0xFF50efe4; // 0.75
                int blue = 0xFF277eff; // 1
                if (percentage <= 0.25F) {
                    color1 = blue;
                    color2 = lightBlue;
                    percentage = (percentage) / 0.25F;
                } else if (percentage <= 0.5F) {
                    color1 = lightBlue;
                    color2 = green;
                    percentage = (percentage - 0.25F) / 0.25F;
                } else if (percentage <= 0.75F) {
                    color1 = green;
                    color2 = yellow;
                    percentage = (percentage - 0.5F) / 0.25F;
                } else {
                    color1 = yellow;
                    color2 = red;
                    percentage = (percentage - 0.75F) / 0.25F;
                }
            }

//            GUI.fillGradient(400 + 13, 205 + 1, 400 + 19, 205 + 31 - 15, startColor, middleColor);
//            GUI.fillGradient(400 + 13, 205 + 1 + 15, 400 + 19, 205 + 31, middleColor, endColor);
            fillMixture(TransformationMatrix.identity().getMatrix(),
                    x1, y1, x2, y2,
                    color1, color2, percentage
            );
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            Minecraft.getInstance().getTextureManager().bindTexture(THERMOMETER_LOCATION);
            drawSprite(400, 205, 0, 0, 0, 32, 32, 32, 32);
            RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}

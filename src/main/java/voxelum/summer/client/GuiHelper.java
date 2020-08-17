package voxelum.summer.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;

public class GuiHelper {
    public static void fillRect(int x1, int y1, int x2, int y2, int color) {
        AbstractGui.fill(x1, y1, x2, y2, color);
    }

    public static void drawString(String text, int x, int y, int color) {
        Minecraft.getInstance().fontRenderer.drawString(text, x, y, color);
    }

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


}

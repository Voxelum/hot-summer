package voxelum.summer;

import net.minecraft.client.Minecraft;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class Debug {
    public static float biomeTemp;
    public static float entityTemp;
    public static float blockTemp;
    public static float bodyTemp;

    @SubscribeEvent
    public static void onRender(RenderGameOverlayEvent.Post event) {
        if (event.getType() == RenderGameOverlayEvent.ElementType.ALL) {
            Minecraft.getInstance().fontRenderer.drawString(String.format("%.2f", bodyTemp), 0, 0, 0);
            Minecraft.getInstance().fontRenderer.drawString(String.format("%.2f", entityTemp) + "", 0, 10, 0);
            Minecraft.getInstance().fontRenderer.drawString(String.format("%.2f", blockTemp), 0, 20, 0);
            Minecraft.getInstance().fontRenderer.drawString(String.format("%.2f", biomeTemp), 0, 30, 0);
        }
    }
}

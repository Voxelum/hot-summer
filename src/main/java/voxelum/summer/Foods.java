package voxelum.summer;

import net.minecraft.item.Food;

public class Foods {
    public static final Food TEA = new Food.Builder()
            .hunger(1)
            .saturation(0.1F)
            .setAlwaysEdible()
            .build();
}

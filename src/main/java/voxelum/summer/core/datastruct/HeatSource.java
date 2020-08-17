package voxelum.summer.core.datastruct;

/**
 * Represent a heat source that can conduct the heat to other heat source by its temperature.
 * This should by hook to the {@link net.minecraft.entity.LivingEntity}
 */
public class HeatSource {
    /**
     * The temperature of this source owner
     */
    public float temperature;
}

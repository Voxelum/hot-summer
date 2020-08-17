package voxelum.summer.core.datastruct;

import voxelum.summer.core.BodyStatusCapability;

/**
 * The capability attach to {@link net.minecraft.entity.LivingEntity}
 * to maintain the status like body temperature and hydration.
 *
 * @see BodyStatusCapability
 */
public class BodyStatus extends HeatSource {
    /**
     * The hydration value range is 0-1
     */
    public float hydration = 1;

    /**
     * The cache of delta temperature of this tick (not affect by the warm keeper and tick factor)
     */
    public float deltaTemperature;

    public void incrementHydration(float hydr) {
        hydration += hydr;
        if (hydration > 1) {
            hydration = 1;
        } else if (hydration < 0) {
            hydration = 0;
        }
    }
}

package voxelum.summer.core;

/**
 * The capability attach to {@link net.minecraft.entity.LivingEntity}
 * to maintain the status like body temperature and hydration.
 *
 * @see BodyStatusCapability
 */
public class BodyStatus extends HeatSource {
    public float hydration = 1;
}

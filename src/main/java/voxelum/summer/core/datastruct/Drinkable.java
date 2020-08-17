package voxelum.summer.core.datastruct;

/**
 * Attach to {@link net.minecraft.item.ItemStack} to make the item stack drive/eat recover hydration
 */
public class Drinkable {
    /**
     * The hydration value to recover.
     *
     * Hydration value range is [0,1]
     */
    public float hydrationRecovery;
}

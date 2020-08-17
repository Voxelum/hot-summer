package voxelum.summer.core.datastruct;

/**
 * Attach to {@link net.minecraft.item.ItemStack} to make the item stack drive/eat recover hydration
 */
public class Drinkable {
    /**
     * The hydration value to recover.
     * <p>
     * Hydration value range is [0,1]
     */
    public float hydrationRecovery = 0.2F;

    /**
     * Is this liquid is dirty or not
     */
    public boolean dirty = true;

    /**
     * How much temperature should add to the drinker.
     * >0 for hot drink
     * =0 for normal drink
     * <0 for cold drink
     */
    public float deltaTemperature = 0;

    /**
     * Is water salty?
     */
    public boolean salty = false;
}

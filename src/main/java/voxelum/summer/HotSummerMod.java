package voxelum.summer;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.*;
import net.minecraft.nbt.INBT;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.world.biome.Biomes;
import net.minecraft.world.gen.GenerationStage;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.IFeatureConfig;
import net.minecraft.world.gen.feature.NoFeatureConfig;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import voxelum.summer.blocks.TeaCropsBlock;
import voxelum.summer.core.*;
import voxelum.summer.core.datastruct.*;
import voxelum.summer.gen.feature.TeaFeature;
import voxelum.summer.items.DrinkableItem;
import voxelum.summer.utils.CapabilityUtils;

import javax.annotation.Nullable;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(HotSummerMod.MODID)
public class HotSummerMod {
    public static final String MODID = "hotsummer";

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();

    public static final DeferredRegister<Effect> EFFECTS_REGISTRY = new DeferredRegister<>(ForgeRegistries.POTIONS, MODID);
    public static final DeferredRegister<Item> ITEMS_REGISTRY = new DeferredRegister<>(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<Block> BLOCKS_REGISTRY = new DeferredRegister<>(ForgeRegistries.BLOCKS, MODID);
    public static final DeferredRegister<Feature<?>> FEATURE_REGISTRY = new DeferredRegister<>(ForgeRegistries.FEATURES, MODID);
    public static final RegistryObject<Block> TEA_CORP_BLOCK = BLOCKS_REGISTRY.register("tea_corp",
            () -> new TeaCropsBlock(Block.Properties.create(Material.PLANTS)
                    .doesNotBlockMovement()
                    .tickRandomly()
                    .hardnessAndResistance(0.0F)
                    .sound(SoundType.CROP)));
    public static final RegistryObject<Item> TEA_ITEM = ITEMS_REGISTRY.register("tea",
            () -> new BlockNamedItem(TEA_CORP_BLOCK.get(), new Item.Properties().group(ItemGroup.FOOD).food(Foods.TEA)));
    public static final RegistryObject<Item> COOKED_TEA_ITEM = ITEMS_REGISTRY.register("cooked_tea",
            () -> new Item(new Item.Properties().group(ItemGroup.FOOD).food(Foods.TEA)));

    public static final RegistryObject<Item> BOTTLE_TEA_ITEM = ITEMS_REGISTRY.register("bottle_tea",
            () -> new DrinkableItem(new Item.Properties().group(ItemGroup.FOOD), Items.GLASS_BOTTLE));

    public static final RegistryObject<Item> HOT_WATER_ITEM = ITEMS_REGISTRY.register("hot_water",
            () -> new DrinkableItem(new Item.Properties().group(ItemGroup.FOOD), Items.GLASS_BOTTLE));
    public static final RegistryObject<Item> ICE_WATER_ITEM = ITEMS_REGISTRY.register("ice_water",
            () -> new DrinkableItem(new Item.Properties().group(ItemGroup.FOOD), Items.GLASS_BOTTLE));
    public static final RegistryObject<Item> SALT_WATER_ITEM = ITEMS_REGISTRY.register("salt_water",
            () -> new DrinkableItem(new Item.Properties().group(ItemGroup.FOOD), Items.GLASS_BOTTLE));

    public static final RegistryObject<Feature<NoFeatureConfig>> TEA_FEATURE = FEATURE_REGISTRY.register("tea_crop", TeaFeature::new);

    public static final RegistryObject<Effect> FREEZING_EFFECT = EFFECTS_REGISTRY.register("freezing",
            () -> new Effect(EffectType.HARMFUL, 0) {
                @Override
                public void performEffect(LivingEntity entityLivingBaseIn, int amplifier) {
                    DamageSource damageSource = new DamageSource("freezing");
                    entityLivingBaseIn.attackEntityFrom(damageSource, 1);
                }
            });

    public static final RegistryObject<Effect> FEVER_EFFECT = EFFECTS_REGISTRY.register("fever",
            () -> new Effect(EffectType.HARMFUL, 0) {
                @Override
                public void performEffect(LivingEntity entityLivingBaseIn, int amplifier) {
                    DamageSource damageSource = new DamageSource("fever");
                    entityLivingBaseIn.attackEntityFrom(damageSource, 1);
                }
            });

    @CapabilityInject(BodyStatus.class)
    public static Capability<BodyStatus> CAPABILITY_BODY_STATUS = null;

    @CapabilityInject(WarmKeeper.class)
    public static Capability<WarmKeeper> CAPABILITY_WARN_KEEPER = null;

    @CapabilityInject(HeatSource.class)
    public static Capability<HeatSource> CAPABILITY_HEAT_SOURCE = null;

    @CapabilityInject(Drinkable.class)
    public static Capability<Drinkable> CAPABILITY_DRINKABLE = null;

    @CapabilityInject(ChunkHeatSources.class)
    public static Capability<ChunkHeatSources> CAPABILITY_CHUNK_HEAT_SOURCES = null;

    public HotSummerMod() {
        ITEMS_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCKS_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
        FEATURE_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        CapabilityManager.INSTANCE.register(BodyStatus.class, BodyStatusCapability.STORAGE, BodyStatus::new);
        CapabilityManager.INSTANCE.register(Drinkable.class, CapabilityUtils.emptyStorage(), Drinkable::new);
        CapabilityManager.INSTANCE.register(HeatSource.class, CapabilityUtils.emptyStorage(), HeatSource::new);
        CapabilityManager.INSTANCE.register(WarmKeeper.class, CapabilityUtils.emptyStorage(), WarmKeeper::new);
        CapabilityManager.INSTANCE.register(ChunkHeatSources.class, CapabilityUtils.emptyStorage(), ChunkHeatSources::new);

        Biomes.MOUNTAINS.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, TEA_FEATURE.get().withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG));
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class RenderTypeRegistry {
        @SubscribeEvent
        public static void onRenderTypeSetup(FMLClientSetupEvent event) {
            RenderTypeLookup.setRenderLayer(TEA_CORP_BLOCK.get(), RenderType.getCutout());
        }
    }
}

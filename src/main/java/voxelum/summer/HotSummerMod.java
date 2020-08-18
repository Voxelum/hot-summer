package voxelum.summer;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.BlockNamedItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.Items;
import net.minecraft.network.PacketBuffer;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraft.util.DamageSource;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
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
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import voxelum.summer.blocks.TeaCropsBlock;
import voxelum.summer.core.BodyStatusCapability;
import voxelum.summer.core.BodyStatusSystem;
import voxelum.summer.core.DrinkableSystem;
import voxelum.summer.core.datastruct.*;
import voxelum.summer.core.message.DrinkWaterMessage;
import voxelum.summer.gen.feature.TeaFeature;
import voxelum.summer.items.DrinkableItem;
import voxelum.summer.utils.CapabilityUtils;
import voxelum.summer.utils.ClientStatusHelper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

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

    public static final RegistryObject<Item> FRESH_WATER_ITEM = ITEMS_REGISTRY.register("fresh_water",
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

    public static final RegistryObject<Effect> DEHYDRATION_EFFECT = EFFECTS_REGISTRY.register("dehydration",
            () -> new Effect(EffectType.HARMFUL, 0) {
                @Override
                public void performEffect(LivingEntity entityLivingBaseIn, int amplifier) {
                    entityLivingBaseIn.getCapability(CAPABILITY_BODY_STATUS).ifPresent((b) -> {
                        b.incrementHydration(-0.1F);
                    });
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

    public static String PROTOCOL_VERSION = "1";
    public static final SimpleChannel NETWORK = NetworkRegistry.newSimpleChannel(new ResourceLocation(MODID, "common"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals);

    public HotSummerMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
        ITEMS_REGISTRY.register(modEventBus);
        BLOCKS_REGISTRY.register(modEventBus);
        FEATURE_REGISTRY.register(modEventBus);
        EFFECTS_REGISTRY.register(modEventBus);
        modEventBus.addListener(this::setup);
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        CapabilityManager.INSTANCE.register(BodyStatus.class, BodyStatusCapability.STORAGE, BodyStatus::new);
        CapabilityManager.INSTANCE.register(Drinkable.class, CapabilityUtils.emptyStorage(), Drinkable::new);
        CapabilityManager.INSTANCE.register(HeatSource.class, CapabilityUtils.emptyStorage(), HeatSource::new);
        CapabilityManager.INSTANCE.register(WarmKeeper.class, CapabilityUtils.emptyStorage(), WarmKeeper::new);
        CapabilityManager.INSTANCE.register(ChunkHeatSources.class, CapabilityUtils.emptyStorage(), ChunkHeatSources::new);

        Biomes.MOUNTAINS.addFeature(GenerationStage.Decoration.VEGETAL_DECORATION, TEA_FEATURE.get().withConfiguration(IFeatureConfig.NO_FEATURE_CONFIG));

        NETWORK.registerMessage(0, BodyStatus.class, (s, packet) -> {
            packet.writeFloat(s.temperature);
            packet.writeFloat(s.hydration);
            packet.writeFloat(s.deltaTemperature);
        }, (b) -> {
            BodyStatus bodyStatus = new BodyStatus();
            bodyStatus.temperature = b.readFloat();
            bodyStatus.hydration = b.readFloat();
            bodyStatus.deltaTemperature = b.readFloat();
            return bodyStatus;
        }, (msg, contextSupplier) -> {
            contextSupplier.get().enqueueWork(() -> ClientStatusHelper.updateClientStatus(msg));
            contextSupplier.get().setPacketHandled(true);
        }, Optional.of(NetworkDirection.PLAY_TO_CLIENT));

        NETWORK.registerMessage(1, DrinkWaterMessage.class, (m, packet) -> {
            packet.writeBlockPos(m.pos);
        }, (p) -> DrinkWaterMessage.of(p.readBlockPos()), (msg, context) -> {
            context.get().enqueueWork(() -> {
                ServerPlayerEntity sender = context.get().getSender();
                World world = sender.world;
                BlockPos pos = msg.pos;
                world.playSound(sender, pos, SoundEvents.ENTITY_GENERIC_DRINK, SoundCategory.BLOCKS, 2F, 1F);
                Drinkable drinkable = new Drinkable();
                DrinkableSystem.decorateDrinkable(drinkable, world, pos);
                DrinkableSystem.drink(sender, sender.getCapability(HotSummerMod.CAPABILITY_BODY_STATUS).orElseThrow(Error::new), drinkable);
            });
            context.get().setPacketHandled(true);
        }, Optional.of(NetworkDirection.PLAY_TO_SERVER));

        dumpBiome();
    }

    static void dumpBiome() {
        StringBuilder builder = new StringBuilder();
        for (Biome biome : Biome.BIOMES) {
            String name = biome.getRegistryName().toString();
            float defaultTemperature = biome.getDefaultTemperature();
            float downfall = biome.getDownfall();
            float defaultBiome = BodyStatusSystem.transformBiomeTemperature(defaultTemperature);
            builder.append(name).append("\t").append(defaultTemperature)
                    .append("\t").append(downfall)
                    .append('\t').append(defaultBiome)
                    .append('\n');

        }
        try {
            Files.write(Paths.get("./dump.txt"), builder.toString().getBytes());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
    public static class RenderTypeRegistry {
        @SubscribeEvent
        public static void onRenderTypeSetup(FMLClientSetupEvent event) {
            RenderTypeLookup.setRenderLayer(TEA_CORP_BLOCK.get(), RenderType.getCutout());
        }
    }
}

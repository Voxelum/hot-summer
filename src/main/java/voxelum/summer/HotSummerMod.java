package voxelum.summer;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.item.BlockNamedItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import voxelum.summer.blocks.CompressionMachineBlock;
import voxelum.summer.blocks.TeaCropsBlock;
import voxelum.summer.bodystatus.BodyStatus;
import voxelum.summer.bodystatus.BodyStatusCapability;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(HotSummerMod.MODID)
public class HotSummerMod {
    public static final String MODID = "hotsummer";

    // Directly reference a log4j logger.
    private static final Logger LOGGER = LogManager.getLogger();
    public static final DeferredRegister<Item> ITEMS_REGISTRY = new DeferredRegister<>(ForgeRegistries.ITEMS, MODID);
    public static final DeferredRegister<Block> BLOCKS_REGISTRY = new DeferredRegister<>(ForgeRegistries.BLOCKS, MODID);
    public static final RegistryObject<Block> TEA_CORP_BLOCK = BLOCKS_REGISTRY.register("tea_corp",
            () -> new TeaCropsBlock(Block.Properties.create(Material.PLANTS)
                    .doesNotBlockMovement()
                    .tickRandomly()
                    .hardnessAndResistance(0.0F)
                    .sound(SoundType.CROP)));
    public static final RegistryObject<Item> TEA_ITEM = ITEMS_REGISTRY.register("tea",
            () -> new BlockNamedItem(TEA_CORP_BLOCK.get(), new Item.Properties().group(ItemGroup.FOOD).food(Foods.TEA)));

    @CapabilityInject(BodyStatus.class)
    public static Capability<BodyStatus> CAPABILITY_BODY_STATUS = null;

    public HotSummerMod() {
        ITEMS_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());
        BLOCKS_REGISTRY.register(FMLJavaModLoadingContext.get().getModEventBus());

        // Register the setup method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);
        // Register the enqueueIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::enqueueIMC);
        // Register the processIMC method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::processIMC);
        // Register the doClientStuff method for modloading
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);

        // Register ourselves for server and other game events we are interested in
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event) {
        CapabilityManager.INSTANCE.register(BodyStatus.class, BodyStatusCapability.STORAGE, BodyStatus::new);
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
    }

    private void enqueueIMC(final InterModEnqueueEvent event) {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("examplemod", "helloworld", () -> {
            LOGGER.info("Hello world from the MDK");
            return "Hello world";
        });
    }

    private void processIMC(final InterModProcessEvent event) {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m -> m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    @Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD,value = Dist.CLIENT)
    public static class RenderTypeRegistry {
        @SubscribeEvent
        public static void onRenderTypeSetup(FMLClientSetupEvent event) {
            RenderTypeLookup.setRenderLayer(TEA_CORP_BLOCK.get(), RenderType.getCutout());
        }
    }
}

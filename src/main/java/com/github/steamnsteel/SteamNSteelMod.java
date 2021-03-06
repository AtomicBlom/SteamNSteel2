package com.github.steamnsteel;

import com.github.steamnsteel.api.steam.ISteamTransportRegistry;
import com.github.steamnsteel.api.steam.SteamNSteelInitializedEvent;
import com.github.steamnsteel.api.steam.SteamTransportStateMachine;
import com.github.steamnsteel.api.steam.SteamTransportWorldStateMachineContainer;
import com.github.steamnsteel.blocks.PipeBlock;
import com.github.steamnsteel.jobs.JobManager;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.IForgeRegistry;
import net.minecraftforge.registries.ObjectHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(Reference.MOD_ID)
public class SteamNSteelMod
{
    // Directly reference a log4j logger.
    public static final Logger LOGGER = LogManager.getLogger();
    public static ISteamTransportRegistry SteamTransportRegistry;
    public static SteamTransportWorldStateMachineContainer SteamTransportStateMachineContainer;
    public static com.github.steamnsteel.jobs.JobManager JobManager;

    public SteamNSteelMod() {
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

    public static void setSteamTransportRegistry(ISteamTransportRegistry steamTransportRegistry) {
        SteamTransportRegistry = steamTransportRegistry;
    }

    private void setup(final FMLCommonSetupEvent event)
    {
        // some preinit code
        LOGGER.info("HELLO FROM PREINIT");
        LOGGER.info("DIRT BLOCK >> {}", Blocks.DIRT.getRegistryName());

        MinecraftForge.EVENT_BUS.post(new SteamNSteelInitializedEvent(new com.github.steamnsteel.api.steam.SteamTransportRegistry()));
        SteamTransportStateMachineContainer = new SteamTransportWorldStateMachineContainer();
        JobManager = new JobManager();
        JobManager.start();
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        // do something that can only be done on the client
        LOGGER.info("Got game settings {}", event.getMinecraftSupplier().get().gameSettings);
    }

    private void enqueueIMC(final InterModEnqueueEvent event)
    {
        // some example code to dispatch IMC to another mod
        InterModComms.sendTo("examplemod", "helloworld", () -> { LOGGER.info("Hello world from the MDK"); return "Hello world";});
    }

    private void processIMC(final InterModProcessEvent event)
    {
        // some example code to receive and process InterModComms from other mods
        LOGGER.info("Got IMC {}", event.getIMCStream().
                map(m->m.getMessageSupplier().get()).
                collect(Collectors.toList()));
    }
    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(FMLServerStartingEvent event) {
        // do something when the server starts
        LOGGER.info("HELLO from server starting");
    }

    // You can use EventBusSubscriber to automatically subscribe events on the contained class (this is subscribing to the MOD
    // Event bus for receiving Registry Events)
    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {
        @SubscribeEvent
        public static void onBlocksRegistry(final RegistryEvent.Register<Block> blockRegistryEvent) {

            final IForgeRegistry<Block> registry = blockRegistryEvent.getRegistry();
            //TODO: Set properties properly
            //TODO: Refactor registration
            registry.register(new PipeBlock(
                    AbstractBlock.Properties.create(Material.IRON, MaterialColor.ORANGE_TERRACOTTA)
            ).setRegistryName("steamnsteel:pipe"));
        }

        @SubscribeEvent
        public static void onItemRegistry(final RegistryEvent.Register<Item> itemRegistryEvent) {
            final IForgeRegistry<Item> registry = itemRegistryEvent.getRegistry();
            //TODO: Set properties properly
            //TODO: Refactor registration
            //TODO: Custom group
            registry.register(new BlockItem(BlockLibrary.pipe, new Item.Properties().group(ItemGroup.MISC)).setRegistryName("steamnsteel:pipe"));
        }
    }

    @ObjectHolder("steamnsteel")
    public static class BlockLibrary {
        public static final PipeBlock pipe;

        static {
            pipe = null;
        }
    }
}

package com.github.steamnsteel.events;

import com.github.steamnsteel.Reference;
import com.github.steamnsteel.SteamNSteelMod;
import com.github.steamnsteel.api.steam.SteamNSteelInitializedEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, modid = Reference.MOD_ID)
public class SteamEvents {
    @SubscribeEvent
    public static void onSteamTransportRegistry(SteamNSteelInitializedEvent event) {
        SteamNSteelMod.setSteamTransportRegistry(event.getSteamTransportRegistry());
    }
}

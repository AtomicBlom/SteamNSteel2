package com.github.steamnsteel.api.steam;

import net.minecraftforge.eventbus.api.Event;

public class SteamNSteelInitializedEvent extends Event {
    private final ISteamTransportRegistry _steamTransportRegistry;

    public SteamNSteelInitializedEvent(ISteamTransportRegistry steamTransportRegistry)
    {
        _steamTransportRegistry = steamTransportRegistry;
    }

    public ISteamTransportRegistry getSteamTransportRegistry()
    {
        return _steamTransportRegistry;
    }
}
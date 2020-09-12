package com.github.steamnsteel.api.steam;

import net.minecraft.util.RegistryKey;
import net.minecraft.world.World;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class SteamTransportWorldStateMachineContainer {
    ConcurrentMap<RegistryKey<World>, SteamTransportStateMachine> worldStateMachines = new ConcurrentHashMap<>();
    public SteamTransportStateMachine getStateMachineForWorld(World world) {
        return worldStateMachines.computeIfAbsent(world.getDimensionKey(), k -> new SteamTransportStateMachine(world));
    }

    public SteamTransportStateMachine getStateMachineForWorld(RegistryKey<World> worldRegistryKey) {
        final SteamTransportStateMachine steamTransportStateMachine = worldStateMachines.get(worldRegistryKey);
        if (steamTransportStateMachine == null) {
            throw new RuntimeException("Attempted to get a state machine for a world that hasn't had any machines registered to it.");
        }
        return steamTransportStateMachine;
    }
}

package com.github.steamnsteel.api.steam;

import com.github.steamnsteel.SteamNSteelMod;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

import java.util.concurrent.ConcurrentHashMap;

public class SteamTransportRegistry implements ISteamTransportRegistry {
    private final ConcurrentHashMap<GlobalPos, SteamTransport> _steamTransports = new ConcurrentHashMap<>();

    public ISteamTransport registerSteamTransport(World world, BlockPos pos, Direction[] initialAllowedDirections)
    {
        GlobalPos globalPos = GlobalPos.getPosition(world.getDimensionKey(), pos);
        SteamTransport result = _steamTransports.computeIfAbsent(globalPos, SteamTransport::new);

        boolean[] allowedDirections = new boolean[6];

        for (Direction initialAllowedDirection : initialAllowedDirections)
        {
            allowedDirections[initialAllowedDirection.getIndex()] = true;
        }

        for (Direction direction : Direction.values())
        {
            boolean canConnect = allowedDirections[direction.getIndex()];
            result.setCanConnect(direction, canConnect);
        }

        SteamNSteelMod.SteamTransportStateMachineContainer.getStateMachineForWorld(world).addTransport(result);
        return result;
    }

    public void destroySteamTransport(World world, BlockPos pos)
    {
        SteamTransport transport;
        GlobalPos steamTransportLocation = GlobalPos.getPosition(world.getDimensionKey(), pos);

        transport = _steamTransports.remove(steamTransportLocation);
        if (transport != null)
        {
            SteamNSteelMod.SteamTransportStateMachineContainer.getStateMachineForWorld(world).removeTransport(transport);
        }
    }

    public ISteamTransport getSteamTransportAtLocation(GlobalPos globalPos)
    {
        return _steamTransports.get(globalPos);
    }
}

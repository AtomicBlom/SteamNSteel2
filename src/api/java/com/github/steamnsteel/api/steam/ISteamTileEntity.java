package com.github.steamnsteel.api.steam;

import net.minecraft.util.Direction;

public interface ISteamTileEntity {
    default Direction[] getValidSteamTransportDirections()
    {
        return new Direction[]
        {
            Direction.DOWN,
                    Direction.UP,
                    Direction.NORTH,
                    Direction.SOUTH,
                    Direction.WEST,
                    Direction.EAST
        };
    }

    /**
     * You should create a SteamTranport in setLocation, I.e,
     * ChildMod.SteamTransportRegistry.registerSteamTransport(x, y, getValidSteamTransportDirections());
     * and return it here.
     * @return the tile entities' Steam Transport.
     */
    ISteamTransport getSteamTransport();
}

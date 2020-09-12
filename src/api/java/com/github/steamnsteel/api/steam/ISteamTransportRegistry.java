package com.github.steamnsteel.api.steam;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ISteamTransportRegistry
{
    ISteamTransport registerSteamTransport(World world, BlockPos pos, Direction[] direction);

    void destroySteamTransport(World world, BlockPos pos);
}
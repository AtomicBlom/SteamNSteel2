package com.github.steamnsteel.blocks.machines;

import com.github.steamnsteel.SteamNSteelMod;
import com.github.steamnsteel.api.steam.ISteamTileEntity;
import com.github.steamnsteel.api.steam.ISteamTransport;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BoilerTileEntity extends TileEntity implements ISteamTileEntity, ITickableTileEntity {
    private ISteamTransport _steamTransport;

    public BoilerTileEntity(TileEntityType<?> tileEntityTypeIn) {
        super(tileEntityTypeIn);
    }

    @Override
    public void setWorldAndPos(World world, BlockPos pos) {
        super.setWorldAndPos(world, pos);
        _steamTransport = SteamNSteelMod.SteamTransportRegistry.registerSteamTransport(world, pos, getValidSteamTransportDirections());
    }

    @Override
    public ISteamTransport getSteamTransport() {
        return _steamTransport;
    }

    @Override
    public void tick() {
        getSteamTransport().addSteam(20);
    }

    @Override
    public void remove() {
        if (world != null) {
            SteamNSteelMod.SteamTransportRegistry.destroySteamTransport(world, pos);
        }
        super.remove();
    }

    @Override
    public Direction[] getValidSteamTransportDirections()
    {
        return new Direction[]
        {
            Direction.NORTH,
                    Direction.SOUTH,
                    Direction.EAST,
                    Direction.WEST,
        };
    }
}

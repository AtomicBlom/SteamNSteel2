package com.github.steamnsteel.blocks.machines;

import com.github.steamnsteel.SteamNSteelMod;
import com.github.steamnsteel.api.steam.ISteamTileEntity;
import com.github.steamnsteel.api.steam.ISteamTransport;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class PipeTileEntity extends TileEntity implements ISteamTileEntity {
    private ISteamTransport _steamTransport;

    public PipeTileEntity(TileEntityType<?> tileEntityTypeIn) {
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
    public void remove() {
        super.remove();
        SteamNSteelMod.SteamTransportRegistry.destroySteamTransport(world, pos);
    }
}

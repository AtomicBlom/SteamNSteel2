package com.github.steamnsteel.blocks.machines;

import com.github.steamnsteel.SteamNSteelMod;
import com.github.steamnsteel.api.steam.ISteamTileEntity;
import com.github.steamnsteel.api.steam.ISteamTransport;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BallMillTileEntity extends TileEntity implements ISteamTileEntity, ITickableTileEntity {

    private ISteamTransport _steamTransport;

    public BallMillTileEntity(TileEntityType<?> tileEntityTypeIn) {
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
    public void tick()
    {
        _steamTransport.takeSteam(2);
    }

    @Override
    public void remove() {
        super.remove();
        SteamNSteelMod.SteamTransportRegistry.destroySteamTransport(world, pos);
    }
}

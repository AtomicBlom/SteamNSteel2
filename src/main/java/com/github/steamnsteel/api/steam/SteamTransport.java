package com.github.steamnsteel.api.steam;

import net.minecraft.util.Direction;
import net.minecraft.util.math.GlobalPos;

import javax.annotation.Nullable;

public class SteamTransport implements ISteamTransport {
    private final GlobalPos _globalPos;

    SteamTransport(GlobalPos globalPos)
    {
        _globalPos = globalPos;
        _maximumSteam = 1000;
        _maximumWater = 800;
    }

    private double _waterStored = 0;
    private double _steamStored = 0;

    private double _temperature;
    private double _heatConductivity;

    private double _maximumWater;
    private double _maximumSteam;

    final ISteamTransport[] _adjacentTransports = new ISteamTransport[6];
    final boolean[] _canConnect = new boolean[6];

    private boolean _debug;
    public boolean StructureChanged;

    public void addSteam(double unitsOfSteam)
    {
        if (_steamStored + unitsOfSteam >= _maximumSteam)
        {
            _steamStored = _maximumSteam;
            return;
        }

        _steamStored += unitsOfSteam;
    }

    public void addCondensate(double unitsOfWater)
    {
        if (_waterStored + unitsOfWater >= _maximumWater)
        {
            _waterStored = _maximumWater;
            return;
        }

        _waterStored += unitsOfWater;
    }

    public double takeSteam(double desiredUnitsOfSteam)
    {
        if (_steamStored <= 0)
        {
            _steamStored = 0;
            return 0;
        }
        if (desiredUnitsOfSteam <= _steamStored)
        {
            _steamStored -= desiredUnitsOfSteam;
            return desiredUnitsOfSteam;
        }

        double actualUnitsOfSteam = _steamStored;
        _steamStored = 0;
        return actualUnitsOfSteam;
    }

    public double takeCondensate(double desiredUnitsOfWater)
    {
        if (_waterStored <= 0)
        {
            _waterStored = 0;
            return 0;
        }

        if (desiredUnitsOfWater <= _waterStored)
        {
            _waterStored -= desiredUnitsOfWater;
            return desiredUnitsOfWater;
        }

        double actualUnitsOfSteam = _waterStored;
        _waterStored = 0;
        return actualUnitsOfSteam;
    }

    public void setMaximumSteam(double maximumUnitsOfSteam)
    {
        _maximumSteam = maximumUnitsOfSteam;
    }

    public void setMaximumCondensate(double maximumUnitsOfWater)
    {
        _maximumWater = maximumUnitsOfWater;
    }

    public void toggleDebug()
    {
        _debug = !_debug;
    }

    public boolean getShouldDebug()
    {
        return _debug;
    }

    public double getSteamStored()
    {
        return _steamStored;
    }

    public double getWaterStored()
    {
        return _waterStored;
    }

    public double getMaximumWater()
    {
        return _maximumWater;
    }

    public double getMaximumSteam()
    {
        return _maximumSteam;
    }

    public double getTemperature()
    {
        return _temperature;
    }

    public void setTemperature(double temperature)
    {
        _temperature = temperature;
    }

    public double getHeatConductivity()
    {
        return _heatConductivity;
    }

    public void setCanConnect(Direction direction, boolean canConnect)
    {
        _canConnect[direction.getIndex()] = canConnect;
    }

    public boolean canConnect(Direction direction)
    {
        return _canConnect[direction.getIndex()];
    }

    public void setAdjacentTransport(Direction direction, @Nullable ISteamTransport transport)
    {
        if (canConnect(direction))

            _adjacentTransports[direction.getIndex()] = transport;
        StructureChanged = true;
    }

    @Nullable
    public ISteamTransport getAdjacentTransport(Direction direction)
    {
        return _adjacentTransports[direction.getIndex()];
    }

    public boolean canTransportAbove()
    {
        return _adjacentTransports[Direction.UP.getIndex()] != null;
    }

    public boolean canTransportBelow()
    {
        return _adjacentTransports[Direction.DOWN.getIndex()] != null;
    }

    public GlobalPos getTransportLocation()
    {
        return _globalPos;
    }
}

package com.github.steamnsteel.api.steam;

import com.github.steamnsteel.SteamNSteelMod;

public class SteamTransportTransientData {
    private Object lockObj = new Object();
    private long tickLastUpdated = 0;
    public SteamTransportTransientData(SteamTransport transport)
    {
        this.transport = transport;
    }

    public void verifyTick(long currentTick)
    {
        synchronized (lockObj)
        {
            if (tickLastUpdated != currentTick)
            {
                _previousState.SteamStored = transport.getSteamStored();
                _previousState.CondensationStored = transport.getWaterStored();
                _previousState.Temperature = transport.getTemperature();
                _previousState.MaximumCondensation = transport.getMaximumWater();
                _previousState.ActualMaximumSteam = SteamMaths.calculateMaximumSteam(
                        _previousState.CondensationStored,
                        transport.getMaximumWater(),
                        transport.getMaximumSteam()
                );
                _previousState.SteamDensity = SteamMaths.calculateSteamDensity(_previousState.SteamStored, _previousState.ActualMaximumSteam);
                _condensationAdded = 0;
                _steamAdded = 0;
                tickLastUpdated = currentTick;
            }
        }
    }

    private final SteamTransport transport;
    private double _condensationAdded;
    private double _steamAdded;
    private final PreviousTransportState _previousState = new PreviousTransportState();

    public PreviousTransportState getPreviousState()
    {
        return _previousState;
    }

    public static class PreviousTransportState
    {
        public double SteamStored;
        public double Temperature;
        public double CondensationStored;
        public double MaximumCondensation;
        public double ActualMaximumSteam;
        public double SteamDensity;
    }

    public double takeSteam(double amount)
    {
        double amountTaken = transport.takeSteam(amount);
        //TODO: subtract from SteamAdded?
        return amountTaken;
    }

    public double takeCondensate(double amount)
    {
        double amountTaken = transport.takeCondensate(amount);
        //TODO: subtract from CondensationAdded?
        return amountTaken;
    }

    public void addCondensate(double waterGained)
    {
        transport.addCondensate(waterGained);
        _condensationAdded += waterGained;
    }

    public void addSteam(double amount)
    {
        transport.addSteam(amount);
        _steamAdded += amount;
    }

    public double getCondensationAdded()
    {
        return _condensationAdded;
    }

    public double getSteamAdded()
    {
        return _steamAdded;
    }

    public double getTemperature()
    {
        return transport.getTemperature();
    }

    public void setTemperature(double value) {
        double temperature = value;
        if (temperature > 100)
        {
            temperature = 100;
        }
        if (temperature < 0)
        {
            temperature = 0;
        }
        transport.setTemperature(temperature);
    }

    public double getUsableSteam()
    {
        return _previousState.SteamStored - _steamAdded;
    }

    public double getUsableWater()
    {
        return _previousState.CondensationStored - _condensationAdded;
    }

    public boolean getDebug()
    {
        return transport.getShouldDebug();
    }

    public long getTickLastUpdated()
    {
        return tickLastUpdated;
    }

    public SteamTransport getTransport()
    {
        return transport;
    }
}

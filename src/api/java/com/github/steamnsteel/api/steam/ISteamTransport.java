package com.github.steamnsteel.api.steam;

public interface ISteamTransport
{
    void addSteam(double unitsOfSteam);

    void addCondensate(double unitsOfWater);

    double takeSteam(double desiredUnitsOfSteam);

    double takeCondensate(double desiredUnitsOfWater);

    void setMaximumSteam(double maximumUnitsOfSteam);

    void setMaximumCondensate(double maximumUnitsOfWater);

    void toggleDebug();

    boolean getShouldDebug();
    double getSteamStored();
    double getWaterStored();
    double getMaximumWater();

    double getMaximumSteam();

    //double GetCalculatedMaximumSteam();
    double getTemperature();


    boolean canTransportAbove();
    boolean canTransportBelow();
}
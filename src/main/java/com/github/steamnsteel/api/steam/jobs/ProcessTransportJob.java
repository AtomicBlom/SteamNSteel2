package com.github.steamnsteel.api.steam.jobs;

import com.github.steamnsteel.SteamNSteelConfiguration;
import com.github.steamnsteel.SteamNSteelMod;
import com.github.steamnsteel.api.steam.*;
import com.github.steamnsteel.jobs.IJob;
import net.minecraft.util.Direction;
import net.minecraft.util.math.GlobalPos;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Stack;

public class ProcessTransportJob implements IJob {
    public final SteamTransport _transport;
    private final INotifyTransportJobComplete _notificationRecipient;
    private final List<SteamTransportTransientData> _eligibleTransportData = new ArrayList<>();
    private final SteamNSteelConfiguration _config;
    private SteamTransportTransientData[] _horizontalAdjacentTransports;
    private SteamTransportTransientData[] _allAdjacentTransports;
    private SteamTransportTransientData _transportData;
    private SteamTransportTransientData _transportAbove;
    private SteamTransportTransientData _transportBelow;
    private long _currentTick;

    public ProcessTransportJob(SteamTransport transport, INotifyTransportJobComplete notificationRecipient, SteamNSteelConfiguration config)
    {
        _transport = transport;
        _notificationRecipient = notificationRecipient;
        _config = config;
    }

    public void execute()
    {
        try
        {
            if (_transportData == null || _transport.StructureChanged)
            {
                updateLocalData();

                _transport.StructureChanged = false;
            }

            _transportData.verifyTick(_currentTick);

            transferSteam();
            calculateUnitHeat();
            transferWater();
            condenseSteam();
        }
        catch (Exception e)
        {
            SteamNSteelMod.LOGGER.error(e);
        }

        _notificationRecipient.jobComplete();
    }

    private void updateLocalData()
    {
        SteamTransportStateMachine stateMachine = SteamNSteelMod.SteamTransportStateMachineContainer.getStateMachineForWorld(_transport.getTransportLocation().getDimension());
        List<SteamTransportTransientData> adjacentTransports = new ArrayList<SteamTransportTransientData>();

        SteamTransport adjacentTransport = (SteamTransport) _transport.getAdjacentTransport(Direction.NORTH);
        if (adjacentTransport != null)
        {
            adjacentTransports.add(stateMachine.getJobDataForTransport(adjacentTransport));
        }
        adjacentTransport = (SteamTransport) _transport.getAdjacentTransport(Direction.EAST);
        if (adjacentTransport != null)
        {
            adjacentTransports.add(stateMachine.getJobDataForTransport(adjacentTransport));
        }
        adjacentTransport = (SteamTransport) _transport.getAdjacentTransport(Direction.SOUTH);
        if (adjacentTransport != null)
        {
            adjacentTransports.add(stateMachine.getJobDataForTransport(adjacentTransport));
        }
        adjacentTransport = (SteamTransport) _transport.getAdjacentTransport(Direction.WEST);
        if (adjacentTransport != null)
        {
            adjacentTransports.add(stateMachine.getJobDataForTransport(adjacentTransport));
        }
        _horizontalAdjacentTransports = adjacentTransports.toArray(new SteamTransportTransientData[0]);

        adjacentTransport = (SteamTransport)_transport.getAdjacentTransport(Direction.UP);
        _transportAbove = adjacentTransport == null ? null : stateMachine.getJobDataForTransport(adjacentTransport);
        if (_transportAbove != null)
        {
            adjacentTransports.add(_transportAbove);
        }
        adjacentTransport = (SteamTransport)_transport.getAdjacentTransport(Direction.DOWN);
        _transportBelow = adjacentTransport == null ? null : stateMachine.getJobDataForTransport(adjacentTransport);
        if (_transportBelow != null)
        {
            adjacentTransports.add(_transportBelow);
        }

        _allAdjacentTransports = adjacentTransports.toArray(new SteamTransportTransientData[0]);
        _transportData = stateMachine.getJobDataForTransport(_transport);
    }

    private void condenseSteam()
    {
        double usableSteam = _transportData.getPreviousState().SteamStored;

        double newCondensation = usableSteam * _config.CondensationRatePerTick * ((100 - _transportData.getPreviousState().Temperature) / 100);
        double takenCondensation = _transportData.takeSteam(newCondensation);
        double waterGained = takenCondensation * _config.SteamToWaterRatio;
        _transportData.addCondensate(waterGained);
    }

    private void calculateUnitHeat()
    {
        double unitTemperature = _transportData.getPreviousState().Temperature;
        double tempDifference = _transportData.getPreviousState().SteamDensity - unitTemperature;

        double temperature = unitTemperature + (_transport.getHeatConductivity() * (tempDifference / 100));
        _transportData.setTemperature(temperature);
    }

    private void transferSteam()
    {
        double usableSteam = _transportData.getPreviousState().SteamStored;

        if (usableSteam <= 0) return;

        transferSteam(usableSteam);
    }

    private void transferSteam(double usableSteam)
    {
        _eligibleTransportData.clear();
        double steamSpaceAvailable = 0;

        for (SteamTransportTransientData neighbourUnit : _allAdjacentTransports)
        {
            //Steam providers can always push?
            double neighbourSteamStored = neighbourUnit.getPreviousState().SteamStored;
            double neighbourMaximumSteam = neighbourUnit.getPreviousState().ActualMaximumSteam;
            if (neighbourSteamStored < neighbourMaximumSteam && neighbourSteamStored < usableSteam)
            {
                _eligibleTransportData.add(neighbourUnit);
                steamSpaceAvailable += (neighbourMaximumSteam - neighbourSteamStored);
            }
        }

        double calculatedSteamDensity = _transportData.getPreviousState().SteamDensity;
        if (_transportBelow != null && calculatedSteamDensity >= _config.EQUILIBRIUM && _transportBelow.getPreviousState().SteamStored < _transportData.getPreviousState().SteamStored)
        {
            double neighbourSteamStored = _transportBelow.getPreviousState().SteamStored;
            double neighbourMaximumSteam = _transportBelow.getPreviousState().ActualMaximumSteam;
            if (neighbourSteamStored < neighbourMaximumSteam && neighbourSteamStored < usableSteam)
            {
                _eligibleTransportData.add(_transportBelow);
                steamSpaceAvailable += (neighbourMaximumSteam - neighbourSteamStored);
            }
        }

        double originalSteamStored = usableSteam;
        for (SteamTransportTransientData neighbourTransport : _eligibleTransportData)
        {
            double neighbourSteamStored = neighbourTransport.getPreviousState().SteamStored;
            double neighbourMaximumSteam = neighbourTransport.getPreviousState().ActualMaximumSteam;

            double ratio = (neighbourMaximumSteam - neighbourSteamStored) / steamSpaceAvailable;

            double amountTransferred = originalSteamStored * ratio;

            if (neighbourSteamStored + amountTransferred > neighbourMaximumSteam)
            {
                amountTransferred = neighbourMaximumSteam - neighbourSteamStored;
            }

            amountTransferred = amountTransferred * _config.TransferRatio;

            amountTransferred = _transportData.takeSteam(amountTransferred);

            neighbourTransport.verifyTick(_currentTick);
            neighbourTransport.addSteam(amountTransferred);
        }
    }

    private void transferWater()
    {
        double usableWater = _transportData.getPreviousState().CondensationStored;

        if (usableWater <= 0)
        {
            transferWaterFromHigherPoint();
            return;
        }
        //First, work on any units above
        if (_transportBelow != null)
        {
            transferWaterBelow(usableWater);
        }

        if (usableWater > 0 && _horizontalAdjacentTransports.length > 0)
        {
            transferWaterAcross(usableWater);
        }

        transferWaterFromHigherPoint();
    }

    private void transferWaterFromHigherPoint()
    {
        if (_transportData.getDebug())
        {
            SteamNSteelMod.LOGGER.debug("HERE! {}", _transport.getTransportLocation());
        }

        if (_transportBelow == null || !(_transportData.getUsableSteam() < _transportData.getPreviousState().ActualMaximumSteam))
        {
            return;
        }
        SteamTransportTransientData.PreviousTransportState previousTransportState = _transportBelow.getPreviousState();
        if (!(Math.abs(previousTransportState.CondensationStored - previousTransportState.MaximumCondensation) < 100))
        {
            return;
        }

        Stack<SearchData> elementsToSearch = new Stack<SearchData>();
        HashSet<GlobalPos> visitedLocations = new HashSet<GlobalPos>();
        visitedLocations.add(_transport.getTransportLocation());
        elementsToSearch.push(new SearchData(_transportBelow.getTransport(), 1));
        SearchData candidate = null;
        Boolean validScenario = true;
        while (validScenario && !elementsToSearch.isEmpty())
        {
            SearchData searchData = elementsToSearch.pop();

            SteamTransport transport = searchData.Transport;
            int depth = searchData.Depth;
            GlobalPos globalPos = transport.getTransportLocation();
            SteamNSteelMod.LOGGER.debug("Checking transport @ {} - {} - {}", globalPos, depth, _transport.getShouldDebug());
            visitedLocations.add(globalPos);

            if (depth <= 0 && (candidate == null || depth < candidate.Depth))
            {
                if (searchData.Depth < 0 || (searchData.Depth == 0 && searchData.Transport.getWaterStored() >= (_transport.getWaterStored() + 5)))
                {
                    candidate = searchData;
                }
            }

            for (Direction direction : Direction.values())
            {
                SteamTransport adjacentTransport = (SteamTransport)transport.getAdjacentTransport(direction);
                if (adjacentTransport != null && !visitedLocations.contains(adjacentTransport.getTransportLocation()))
                {
                    SteamTransportTransientData steamTransportTransientData = SteamNSteelMod.SteamTransportStateMachineContainer.getStateMachineForWorld(_transportData.getTransport().getTransportLocation().getDimension()).getJobDataForTransport(adjacentTransport);
                    SteamTransportTransientData.PreviousTransportState nextPreviousData = steamTransportTransientData.getPreviousState();

                    if ((direction == Direction.EAST || direction == Direction.WEST || direction == Direction.NORTH ||
                            direction == Direction.SOUTH) &&
                            nextPreviousData.CondensationStored < nextPreviousData.MaximumCondensation - 10)
                    {
                        validScenario = false;
                        break;
                    }

                    if (nextPreviousData.CondensationStored > 10)
                    {
                        int newDepth = depth + direction.getYOffset();
                        elementsToSearch.push(new SearchData(adjacentTransport, newDepth));
                    }
                }
            }
        }

        if (candidate != null)
        {
            SteamNSteelMod.LOGGER.debug("Updating from candidate {} - {}", candidate.Transport.getTransportLocation(), candidate.Depth);
            double condensate;
            if (candidate.Depth == 0)
            {
                condensate = candidate.Transport.getWaterStored() / 2;
                if (condensate > 100)
                {
                    condensate = 100;
                }
            }
            else
            {
                condensate = 100;
            }

            double actualCondensate = candidate.Transport.takeCondensate(condensate);
            _transportData.addCondensate(actualCondensate);
        }
    }

    private void transferWaterAcross(double waterUsedAtStart)
    {
        _eligibleTransportData.clear();

        if (_horizontalAdjacentTransports.length == 0)
        {
            return;
        }

        //FIXME: Ensure that Integer.MAX_VALUE is all 1s
        int elementIndex = (int)(_transportData.getTickLastUpdated() & Integer.MAX_VALUE) % _horizontalAdjacentTransports.length;
        SteamTransportTransientData nextTransport = _horizontalAdjacentTransports[elementIndex];

        if (nextTransport == null)
        {
            return;
        }
        nextTransport.verifyTick(_currentTick);

        double neighbourWaterStored = nextTransport.getPreviousState().CondensationStored;
        double neighbourMaximumWater = nextTransport.getPreviousState().MaximumCondensation;
        if (neighbourWaterStored >= neighbourMaximumWater || !(neighbourWaterStored < waterUsedAtStart))
        {
            return;
        }

        double waterStored = _transportData.getPreviousState().CondensationStored;
        if (neighbourWaterStored >= waterStored)
        {
            return;
        }

        double desiredTransfer = (waterStored - neighbourWaterStored)/(_horizontalAdjacentTransports.length + 1);
        for (SteamTransportTransientData steamTransportTransientData : _horizontalAdjacentTransports)
        {
            double takeCondensate = _transportData.takeCondensate(desiredTransfer);
            steamTransportTransientData.addCondensate(takeCondensate);
        }
    }

    private void transferWaterBelow(double usableWater)
    {
        double neighbourWaterStored = _transportBelow.getPreviousState().CondensationStored;
        double neighbourMaximumWater = _transportBelow.getPreviousState().MaximumCondensation;

        if (!(neighbourWaterStored < neighbourMaximumWater)) return;

        double amountTransferred = usableWater;

        if (neighbourWaterStored + amountTransferred > neighbourMaximumWater)
        {
            amountTransferred = neighbourMaximumWater - neighbourWaterStored;
        }

        if (usableWater - amountTransferred < 0)
        {
            amountTransferred = usableWater;
        }

        amountTransferred = _transportData.takeCondensate(amountTransferred);

        _transportBelow.verifyTick(_currentTick);
        _transportBelow.addCondensate(amountTransferred);
    }

    public void setCurrentTick(long currentTick) {
        _currentTick = currentTick;
    }

    private class SearchData
    {
        final SteamTransport Transport;
        final int Depth;

        public SearchData(ISteamTransport transport, int depth)
        {
            Transport = (SteamTransport)transport;
            Depth = depth;
        }
    }
}

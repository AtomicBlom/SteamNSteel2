package com.github.steamnsteel.api.steam;

import com.github.steamnsteel.SteamNSteelConfiguration;
import com.github.steamnsteel.SteamNSteelMod;
import com.github.steamnsteel.api.steam.jobs.ProcessTransportJob;
import com.github.steamnsteel.api.steam.jobs.RegisterTransportJob;
import com.github.steamnsteel.api.steam.jobs.UnregisterTransportJob;
import net.minecraft.util.Direction;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.world.World;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.atomic.AtomicInteger;

public class SteamTransportStateMachine implements INotifyTransportJobComplete {
    private final World _world;

    public SteamTransportStateMachine(World world)
    {
        _world = world;
        _steamNSteelConfiguration = new SteamNSteelConfiguration();
    }

    private Map<GlobalPos, ProcessTransportJob> IndividualTransportJobs = new HashMap<GlobalPos, ProcessTransportJob>();
    private Map<ISteamTransport, SteamTransportTransientData> TransientData = new HashMap<ISteamTransport, SteamTransportTransientData>();
    private CyclicBarrier barrier = new CyclicBarrier(2);
    private SteamNSteelConfiguration _steamNSteelConfiguration;
    private AtomicInteger expectedJobs;
    private boolean expectingJobs;

    public void onTick()
    {
        processTransports();
    }

    private void processTransports()
    {
        if (expectedJobs.get() > 0)
        {
            throw new RuntimeException("Attempt to run a second tick with already outstanding jobs?");
        }
        Collection<ProcessTransportJob> jobs = IndividualTransportJobs.values();
        if (jobs.isEmpty())
        {
            expectingJobs = false;
            return;
        }

        expectedJobs.set(jobs.size());
        for (ProcessTransportJob job : jobs)
        {
            job.setCurrentTick(_world.getGameTime());
            SteamNSteelMod.JobManager.addBackgroundJob(job);
        }

        expectingJobs = true;
    }

    public void postTick()
    {
        if (expectingJobs)
        {
            SteamNSteelMod.LOGGER.debug("{} Waiting postTick", _world.getGameTime());
            try {
                //FIXME: Check these properly
                barrier.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (BrokenBarrierException e) {
                e.printStackTrace();
            }
            SteamNSteelMod.LOGGER.debug("{} finished postTick", _world.getGameTime());
        }
    }

    private void finished()
    {
        SteamNSteelMod.LOGGER.debug("{} Waiting PostJobs", _world.getGameTime());
        try {
            //FIXME: Check these properly
            barrier.await();
        } catch (InterruptedException | BrokenBarrierException e) {
            e.printStackTrace();
        }
        SteamNSteelMod.LOGGER.debug("{} Released PostJobs", _world.getGameTime());
    }

    void addTransport(SteamTransport transport)
    {
        SteamNSteelMod.JobManager.addPreTickJob(new RegisterTransportJob(this, transport));
    }

    void removeTransport(SteamTransport transport)
    {
        SteamNSteelMod.JobManager.addPreTickJob(new UnregisterTransportJob(this, transport));
    }

    public void addTransportInternal(SteamTransport transport)
    {
        GlobalPos globalPos = transport.getTransportLocation();
        SteamNSteelMod.LOGGER.debug("{} Adding Transport {}", _world.getGameTime(), globalPos);
        TransientData.put(transport, new SteamTransportTransientData(transport));

        for (Direction direction : Direction.values())
        {
            if (!transport.canConnect(direction)) continue;
            GlobalPos altGlobalPos = GlobalPos.getPosition(globalPos.getDimension(), globalPos.getPos().offset(direction));

            ProcessTransportJob foundTransportJob;
            foundTransportJob = IndividualTransportJobs.get(altGlobalPos);
            if (foundTransportJob == null) continue;
            SteamTransport foundTransport = foundTransportJob._transport;
            Direction oppositeDirection = direction.getOpposite();
            if (!foundTransport.canConnect(oppositeDirection)) continue;

            transport.setAdjacentTransport(direction, foundTransport);
            foundTransport.setAdjacentTransport(oppositeDirection, transport);
        }

        IndividualTransportJobs.put(globalPos, new ProcessTransportJob(transport, this, _steamNSteelConfiguration));
    }

    public void removeTransportInternal(SteamTransport transport)
    {
        IndividualTransportJobs.remove(transport.getTransportLocation());
        TransientData.remove(transport);

        for (Direction direction : Direction.values())
        {
            SteamTransport adjacentTransport = (SteamTransport)transport.getAdjacentTransport(direction);
            if (adjacentTransport == null) continue;

            adjacentTransport.setAdjacentTransport(direction.getOpposite(), null);
        }
    }

    public SteamTransportTransientData getJobDataForTransport(ISteamTransport processTransportJob)
    {
        return TransientData.get(processTransportJob);
    }

    public void jobComplete()
    {
        if (expectedJobs.decrementAndGet() == 0)
        {
            finished();
        }
    }
}

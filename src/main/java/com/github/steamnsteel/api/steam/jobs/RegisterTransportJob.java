package com.github.steamnsteel.api.steam.jobs;

import com.github.steamnsteel.api.steam.SteamTransport;
import com.github.steamnsteel.api.steam.SteamTransportStateMachine;
import com.github.steamnsteel.jobs.IJob;

public class RegisterTransportJob implements IJob {
    private final SteamTransportStateMachine _steamTransportStateMachine;
    private final SteamTransport _transport;

    public RegisterTransportJob(SteamTransportStateMachine steamTransportStateMachine, SteamTransport transport)
    {
        _steamTransportStateMachine = steamTransportStateMachine;
        _transport = transport;
    }

    public void execute()
    {
        _steamTransportStateMachine.addTransportInternal(_transport);
    }
}

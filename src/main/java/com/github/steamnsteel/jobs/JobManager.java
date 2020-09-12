package com.github.steamnsteel.jobs;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingDeque;

public class JobManager implements IJobManager {
    private final List<Thread> JobThreads = new ArrayList<Thread>();
    private boolean running = true;
    private BlockingQueue<IJob> _backgroundJobs = new LinkedBlockingDeque<IJob>();
    private ConcurrentLinkedQueue<IJob> _pretickJobs = new ConcurrentLinkedQueue<IJob>();

    public void addBackgroundJob(IJob job)
    {
        try {
            //FIXME: Handle this properly.
            _backgroundJobs.put(job);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void addPreTickJob(IJob job)
    {
        _pretickJobs.add(job);
    }

    public void doPretickJobs()
    {
        while (!_pretickJobs.isEmpty())
        {
            _pretickJobs.poll().execute();
        }
    }

    public void start()
    {
        stop();
        _backgroundJobs = new LinkedBlockingDeque<IJob>();
        running = true;
        //int processorCount = Environment.ProcessorCount;
        int processorCount = 1;
        for (int i = 0; i < processorCount; ++i)
        {
            Thread t = new Thread(this::startJobThread);
            t.setName("Job Thread #" + i);
            JobThreads.add(t);
            t.start();
        }
    }

    public void stop()
    {
        running = false;
        _backgroundJobs.CompleteAdding();
        for (Thread thread : JobThreads)
        {
            thread.join();
        }
        JobThreads.clear();
    }

    private void startJobThread()
    {
        while (running)
        {
            for (IJob job : _backgroundJobs.GetConsumingEnumerable())
            {
                job.execute();
            }
        }
    }
}

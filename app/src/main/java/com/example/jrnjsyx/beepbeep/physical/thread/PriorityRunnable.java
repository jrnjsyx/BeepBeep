package com.example.jrnjsyx.beepbeep.physical.thread;

/**
 * Created by cc on 2016/10/13.
 */

import android.os.Process;

import com.example.jrnjsyx.beepbeep.physical.ProcessPriority;

/**
 * Runnable that set thread priority before execution. See {@link Process} for list of available priority levels.
 */
public abstract class PriorityRunnable implements Runnable {
    private int threadPriority;

    public PriorityRunnable() {
        threadPriority = Process.THREAD_PRIORITY_BACKGROUND;
    }

    public PriorityRunnable(@ProcessPriority int threadPriority) {
        this.threadPriority = threadPriority;
    }

    @Override
    public void run() {
        Process.setThreadPriority(threadPriority);
        runImpl();
    }

    protected abstract void runImpl();
}
package com.rasppi.jobs;

import com.rasppi.BuildStatus;
import com.rasppi.DummyLightController;
import com.rasppi.LightController;
import com.rasppi.RasppiLightController;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * This class will periodically inspect the message queue and do the lighting.
 * To change this template use File | Settings | File Templates.
 */
public class LightingManagerJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        JobDataMap jobData = context.getJobDetail().getJobDataMap();
        boolean isDebug = jobData.getBoolean("isDebug");
        ConcurrentLinkedDeque<String> msgQueue = (ConcurrentLinkedDeque<String>) jobData.get("msgQueue");

        LightController lightController = (isDebug)
                                                ? DummyLightController.getLightControllerInstance()
                                                : RasppiLightController.getLightControllerInstance();

        String lastBuildEvent;
        do {
            lastBuildEvent = msgQueue.pop();
        }
        while(!msgQueue.isEmpty());

        if (lastBuildEvent != null){
            // 2. When you have found tha lst message, use it to control the light.
            switch (BuildStatus.getBuildStatus(lastBuildEvent)) {
                case ABORTED_BUILDING:
                    lightController.startBlinking();
                    break;
                case BLUE_BUILDING:
                    lightController.startBlinking();
                    break;
                case RED_BUILDING:
                    lightController.startBlinking();
                    break;
                case STABLE:
                    lightController.stopBlinking();
                    lightController.switchOnGreen();
                    break;
                case BROKEN:
                    lightController.stopBlinking();
                    lightController.switchOnRed();
                    break;
                case ABORTED:
                    lightController.stopBlinking();
                    lightController.switchOnRed();
                    break;
                default:
                    // Indicates an unknown state.
                    //lightController.switchOnBoth();
            }
        }
    }
}

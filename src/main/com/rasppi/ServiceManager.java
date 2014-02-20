package com.rasppi;

import org.quartz.SchedulerException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Listen to keyboard interrupt to stop the build monitor.
 * User: Jerome Anthony
 * Date: 10/02/2014
 */
public class ServiceManager implements  Runnable{
    private JenkinsBuildMonitor monitor;

    public ServiceManager(JenkinsBuildMonitor monitor){
        this.monitor = monitor;
    }

    @Override
    public void run() {
        BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
        String input = null;
        while(true){
            try {
                input = in.readLine();
                if (input.equalsIgnoreCase("q")){
                    monitor.stop();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            catch (SchedulerException e) {
                e.printStackTrace();
            }
            finally{
                break;
            }
        };
    }
}

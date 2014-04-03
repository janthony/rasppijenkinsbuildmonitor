package com.rasppi;

import com.rasppi.jobs.JenkinsBuildMonitorJob;
import com.rasppi.jobs.LightingManagerJob;
import org.apache.http.config.ConnectionConfig;
import org.apache.http.impl.nio.DefaultHttpClientIODispatch;
import org.apache.http.impl.nio.pool.BasicNIOConnPool;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.protocol.HttpAsyncRequestExecutor;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.nio.reactor.IOEventDispatch;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.protocol.*;
import org.quartz.*;
import org.quartz.impl.StdSchedulerFactory;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * Created with IntelliJ IDEA.
 * User: Jerome Anthony
 * Date: 7/02/2014
 * Time: 11:26 AM
 */
public class JenkinsBuildMonitor {

    private RasppiLightController lightController;
    private Scheduler scheduler;
    private ConnectingIOReactor ioReactor;

    public static void main(String[] args) throws SchedulerException {
        JenkinsBuildMonitor monitor = new JenkinsBuildMonitor();
        try {
            monitor.init();
        } catch (IOReactorException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public void init() throws SchedulerException, IOReactorException {
        // Initialize thredd pool.
        // Create HTTP protocol processing chain
        HttpProcessor httpproc = HttpProcessorBuilder.create()
                // Use standard client-side protocol interceptors
                .add(new RequestContent())
                .add(new RequestTargetHost())
                .add(new RequestConnControl())
                .add(new RequestUserAgent("raspberrypi/1.1"))
                .add(new RequestExpectContinue(true)).build();
        // Create client-side HTTP protocol handler
        HttpAsyncRequestExecutor protocolHandler = new HttpAsyncRequestExecutor();
        // Create client-side I/O event dispatch
        final IOEventDispatch ioEventDispatch = new DefaultHttpClientIODispatch(protocolHandler,
                ConnectionConfig.DEFAULT);
        // Create client-side I/O reactor
        final ConnectingIOReactor ioReactor = new DefaultConnectingIOReactor();

        // Create HTTP connection pool
        BasicNIOConnPool pool = new BasicNIOConnPool(ioReactor, ConnectionConfig.DEFAULT);
        // Limit total number of connections to just two
        pool.setDefaultMaxPerRoute(2);
        pool.setMaxTotal(2);
        // Run the I/O reactor in a separate thread
        Thread t = new Thread(new Runnable() {

            public void run() {
                try {
                    // Ready to go!
                    ioReactor.execute(ioEventDispatch);
                }
                catch (InterruptedIOException ex) {
                    System.err.println("Interrupted");
                }
                catch (IOException e) {
                    System.err.println("I/O error: " + e.getMessage());
                }
                System.out.println("Shutdown");
            }
        });
        // Start the client thread
        t.start();

        // Setup the scheduled job to run.

        ConcurrentLinkedDeque<String> messageQueue = new ConcurrentLinkedDeque<String>();

        JobDataMap jobData = new JobDataMap();
        jobData.put("jobStatus", "unknown");
        jobData.put("ioReactor", ioReactor);
        jobData.put("httpProc", httpproc);
        jobData.put("nioPool", pool);
        jobData.put("msgQueue", messageQueue);
        jobData.put("isDebug", false);
        SchedulerFactory sf = new StdSchedulerFactory();
        Scheduler scheduler = sf.getScheduler();

        scheduleBuildMonitorJob(scheduler, jobData);
        scheduleLightManagerJob(scheduler, jobData);

        scheduler.start();

        // Start the service manager thread. This will manage shutting down the service on user keyboard interrupt.
        ServiceManager serviceManager = new ServiceManager(this);
        new Thread(serviceManager).start();
    }

    private void scheduleBuildMonitorJob(Scheduler scheduler, JobDataMap jobData) throws SchedulerException {
        JobDetail job = JobBuilder.newJob(JenkinsBuildMonitorJob.class)
                .withIdentity("monitor", "jenkins")
                .setJobData(jobData)
                .build();

        Trigger trigger = TriggerBuilder
                .newTrigger()
                .withIdentity("monitor","jenkins")
                .startNow()
                .withSchedule(SimpleScheduleBuilder
                        .repeatSecondlyForever(5)) // Every 30 seconds.
                .build();

        scheduler.scheduleJob(job, trigger);
    }

    private void scheduleLightManagerJob(Scheduler scheduler, JobDataMap jobData) throws SchedulerException {
        JobDetail job = JobBuilder.newJob(LightingManagerJob.class)
                .withIdentity("monitor", "raspi")
                .setJobData(jobData)
                .build();

        Trigger trigger = TriggerBuilder
                .newTrigger()
                .withIdentity("monitor","raspi")
                .startNow()
                .withSchedule(SimpleScheduleBuilder
                        .repeatSecondlyForever(5)) // Every 30 seconds.
                .build();

        scheduler.scheduleJob(job, trigger);
    }


    public void stop() throws IOException, SchedulerException {
        scheduler.shutdown();
        lightController.stop();
        ioReactor.shutdown();
    }
}

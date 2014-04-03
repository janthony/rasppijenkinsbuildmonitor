package com.rasppi.jobs;

import com.rasppi.RasppiLightController;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.concurrent.FutureCallback;
import org.apache.http.impl.nio.pool.BasicNIOConnPool;
import org.apache.http.message.BasicHttpRequest;
import org.apache.http.nio.protocol.BasicAsyncRequestProducer;
import org.apache.http.nio.protocol.BasicAsyncResponseConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequester;
import org.apache.http.nio.reactor.ConnectingIOReactor;
import org.apache.http.protocol.*;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.CountDownLatch;

/**
 * Scheduled job which checks the Jenkins server status and blinks the lights accordingly.
 * User: Jerome Anthonys
 * Date: 9/02/2014
 */
public class JenkinsBuildMonitorJob implements Job {

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {

        JobDataMap jobDataMap = context.getJobDetail().getJobDataMap();
        final ConcurrentLinkedDeque<String> msgQueue = (ConcurrentLinkedDeque<String>) jobDataMap.get("msgQueue");
        ConnectingIOReactor ioReactor = (ConnectingIOReactor) jobDataMap.get("ioReactor");

        // Create HTTP protocol processing chain
        HttpProcessor httpproc = (HttpProcessor) jobDataMap.get("httpProc");
        // Create HTTP requester
        HttpAsyncRequester requester = new HttpAsyncRequester(httpproc);
        BasicNIOConnPool pool = (BasicNIOConnPool) jobDataMap.get("nioPool");
        RasppiLightController lightController = (RasppiLightController) jobDataMap.get("lightController");

        // Execute HTTP GETs to the following hosts and
        HttpHost[] targets = new HttpHost[]{
                new HttpHost("jenkins.cubecloud.com.au")
        };
        final CountDownLatch latch = new CountDownLatch(targets.length);
        for (final HttpHost target : targets) {
            BasicHttpRequest request = new BasicHttpRequest("GET", "/job/Oscar.NET/api/xml?xpath=(//color)[1]");
            HttpCoreContext coreContext = HttpCoreContext.create();
            requester.execute(
                    new BasicAsyncRequestProducer(target, request),
                    new BasicAsyncResponseConsumer(),
                    pool,
                    coreContext,
                    // Handle HTTP response from a callback
                    new FutureCallback<HttpResponse>() {
                        // TODO: Figure out a way to access the parent class properties. We should be able to insert this with references.

                        public void completed(final HttpResponse response) {
                            latch.countDown();
                            System.out.println(target + "->" + response.getStatusLine());
                            try {
                                String status = IOUtils.toString(response.getEntity().getContent());
                                System.out.println(status);
                                msgQueue.add(status);
                            }
                            catch (Exception e){
                                System.out.println(e);
                            }

//                            RasppiLightController lightController = RasppiLightController.getLightControllerInstance();
//
//                            try {
//                                String status = IOUtils.toString(response.getEntity().getContent());
//                                System.out.println("Status: " + status);
//                                switch (BuildStatus.getBuildStatus(status)) {
//                                    case BLUE_BUILDING:
//                                        lightController.startBlinking();
//                                        break;
//                                    case RED_BUILDING:
//                                        lightController.startBlinking();
//                                        break;
//                                    case STABLE:
//                                        lightController.stopBlinking();
//                                        lightController.switchOnGreen();
//                                        break;
//                                    case BROKEN:
//                                        lightController.stopBlinking();
//                                        lightController.switchOnRed();
//                                        break;
//                                    default:
//                                        // Indicates an unknown state.
//                                        //lightController.switchOnBoth();
//                                }
//
//                            } catch (IOException e) {
//                                System.out.println(e);
//                            }
                        }

                        public void failed(final Exception ex) {
                            latch.countDown();
                            System.out.println(target + "->" + ex);
                        }

                        public void cancelled() {
                            latch.countDown();
                            System.out.println(target + " cancelled");
                        }
                    });
        }
    }
}

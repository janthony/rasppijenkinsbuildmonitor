package com.rasppi;

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

import java.io.IOException;
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
        ConnectingIOReactor ioReactor = (ConnectingIOReactor) jobDataMap.get("ioReactor");

        // Create HTTP protocol processing chain
        HttpProcessor httpproc = (HttpProcessor) jobDataMap.get("httpProc");
        // Create HTTP requester
        HttpAsyncRequester requester = new HttpAsyncRequester(httpproc);
        BasicNIOConnPool pool = (BasicNIOConnPool) jobDataMap.get("nioPool");
        LightController lightController = (LightController) jobDataMap.get("lightController");

        // Execute HTTP GETs to the following hosts and
        HttpHost[] targets = new HttpHost[]{
                new HttpHost("cubeclouddev.geoplex.net.au", 8080, "http")
        };
        final CountDownLatch latch = new CountDownLatch(targets.length);
        for (final HttpHost target : targets) {
            BasicHttpRequest request = new BasicHttpRequest("GET", "/job/cubecloud/api/xml?xpath=//color");
            HttpCoreContext coreContext = HttpCoreContext.create();
            requester.execute(
                    new BasicAsyncRequestProducer(target, request),
                    new BasicAsyncResponseConsumer(),
                    pool,
                    coreContext,
                    // Handle HTTP response from a callback
                    new FutureCallback<HttpResponse>() {

                        public void completed(final HttpResponse response) {
                            latch.countDown();
                            System.out.println(target + "->" + response.getStatusLine());

                            LightController lightController = LightController.getLightControllerInstance();

                            try {
                                String status = IOUtils.toString(response.getEntity().getContent());
                                switch (BuildStatus.getBuildStatus(status)) {
                                    case BUILDING:
                                        lightController.startBlinking();
                                    case STABLE:
                                        lightController.stopBlinking();
                                        lightController.switchOnGreen();
                                    case BROKEN:
                                        lightController.stopBlinking();
                                        lightController.switchOnRed();
                                    default:
                                        // Indicates an unknown state.
                                        //lightController.switchOnBoth();
                                }

                            } catch (IOException e) {
                                System.out.println(e);
                            }
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

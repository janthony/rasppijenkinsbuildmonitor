package com.rasppi;

import com.pi4j.io.gpio.*;

/**
 * Created with IntelliJ IDEA.
 * User: jerome
 * Date: 9/02/2014
 * Time: 4:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class DummyLightController implements LightController {

    private static GpioController gpio = null;
    private static GpioPinDigitalOutput redLightPin = null;
    private static GpioPinDigitalOutput greenLightPin = null;
    private static DummyLightController instance = null;
    private static boolean green = false;
    private static boolean red = false;
    private static boolean blinking = false;

    private DummyLightController(boolean dummy){
        System.out.println("Dummy light controller instance intialized.");
    }

    public static DummyLightController getLightControllerInstance(){
        if (instance == null){
           System.out.println("Making  a new light controller instance");
            instance = new DummyLightController(false);
        }

        return instance;
    }

    public void switchOnGreen(){
        System.out.println("Green ON");
    }

    public void switchOffGreen(){
        System.out.println("Green ON");
    }

    public void switchOnRed(){
        System.out.println("Red ON");
    }

    public void switchOffRed(){
        System.out.println("Red ON");
    }

    public void startBlinking(){
        if (!blinking){
            blinking = true;
        }
    }

    public void switchOnBoth(){
        System.out.println("Both on");
    }

    public void switchOffBoth(){
        System.out.println("Both off");
    }

    public void stopBlinking(){
        if (blinking){
            switchOffBoth();
            blinking = false;
        }
        System.out.println("Stopped Blinking");
    }

    public void switchOffAll(){
        System.out.println("Switching off");
    }

    public void stop(){
        System.out.println("Stopped");
        gpio.shutdown();
    }
}

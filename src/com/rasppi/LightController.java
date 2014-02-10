package com.rasppi;

import com.pi4j.io.gpio.*;

/**
 * Created with IntelliJ IDEA.
 * User: jerome
 * Date: 9/02/2014
 * Time: 4:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class LightController {

    private static GpioController gpio = null;
    private static GpioPinDigitalOutput redLightPin = null;
    private static GpioPinDigitalOutput greenLightPin = null;
    private static LightController instance = null;
    private static boolean green = false;
    private static boolean red = false;

    // Configure GPIO.
    private LightController(){
//        gpio = GpioFactory.getInstance();
//        redLightPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_04, "RED", PinState.HIGH); // Off state
//        greenLightPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_06, "GREEN", PinState.HIGH); // Off state
    }

    public static LightController getLightControllerInstance(){
        if (instance == null){
            instance = new LightController();
        }

        return instance;
    }

    public void toggleGreen(){
        this.green = !green;
        System.out.println(green);
    }

    public void toggleRed(){
        this.red = !red;
        System.out.println(red);
    }

    public void startBlinking(){
        System.out.println("Blinking");
    }

    public void stopBlinking(){
        System.out.println("Stopped Blinking");
    }

    public void switchOffAll(){
        System.out.println("Switching off");
    }

    public void stop(){
        System.out.println("Stopped");
//        gpio.shutdown();
    }
}

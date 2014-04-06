package com.rasppi;

import com.pi4j.io.gpio.*;

/**
 * Created with IntelliJ IDEA.
 * User: jerome
 * Date: 9/02/2014
 * Time: 4:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class RasppiLightController implements LightController{

    private static GpioController gpio = null;
    private static GpioPinDigitalOutput redLightPin = null;
    private static GpioPinDigitalOutput greenLightPin = null;
    private static RasppiLightController instance = null;
    private static boolean green = false;
    private static boolean red = false;
    private static boolean blinking = false;

    // Configure GPIO.
    private RasppiLightController(){
        gpio = GpioFactory.getInstance();
        redLightPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "RED", PinState.HIGH); // Off state
        greenLightPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "GREEN", PinState.HIGH); // Off state
    }

    public static RasppiLightController getLightControllerInstance(){
        if (instance == null){
           System.out.println("Making  a new light controller instance");
            instance = new RasppiLightController();
        }

        return instance;
    }

    public void switchOnGreen(){
        greenLightPin.setState(PinState.LOW); // switch off red
        System.out.println("Green ON");
    }

    public void switchOffGreen(){
        greenLightPin.setState(PinState.HIGH); // switch off red
        System.out.println("Green ON");
    }

    public void switchOnRed(){
        redLightPin.setState(PinState.LOW); // switch on red
        System.out.println("Red ON");
    }

    public void switchOffRed(){
        redLightPin.setState(PinState.HIGH); // switch on red
        System.out.println("Red ON");
    }

    public void startBlinking(){
        if (!blinking){
            redLightPin.blink(1000);
            greenLightPin.blink(1000);
            blinking = true;
        }
    }

    public void switchOnBoth(){
        greenLightPin.setState(PinState.LOW); // off green
        redLightPin.setState(PinState.LOW); // switch on red
        System.out.println("Both on");
    }

    public void switchOffBoth(){
        switchOffRed();
        switchOffGreen();
        redLightPin.clearProperties();
        greenLightPin.clearProperties();

        System.out.println("Both off");
    }

    public void stopBlinking(){
        switchOffBoth();
        blinking = false;
        System.out.println("Stopped Blinking");
    }

    public void switchOffAll(){
        redLightPin.clearProperties();
        greenLightPin.clearProperties();
        System.out.println("Switching off");
    }

    public void stop(){
        redLightPin.clearProperties();
        greenLightPin.clearProperties();
        System.out.println("Stopped");
        gpio.shutdown();
    }
}

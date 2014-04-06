package com.rasppi;

import com.pi4j.io.gpio.*;

/**
 * Created with IntelliJ IDEA.
 * User: jerome
 * Date: 9/02/2014
 * Time: 4:25 PM
 * To change this template use File | Settings | File Templates.
 */
public class RasppiLightController implements LightController {

    private static GpioController gpio = null;
    private static GpioPinDigitalOutput redLightPin = null;
    private static GpioPinDigitalOutput greenLightPin = null;
    private static RasppiLightController instance = null;
    private static boolean green = false;
    private static boolean red = false;
    private static boolean blinking = false;

    private PinState PIN_STATE_ON = PinState.LOW;
    private PinState PIN_STATE_OFF = PinState.HIGH;


    // Configure GPIO.
    private RasppiLightController(){
        gpio = GpioFactory.getInstance();
        redLightPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_00, "RED", PIN_STATE_ON); // ON state
        greenLightPin = gpio.provisionDigitalOutputPin(RaspiPin.GPIO_01, "GREEN", PIN_STATE_ON); // ON state
        switchOffRed();
        switchOffGreen();
    }

    public static RasppiLightController getLightControllerInstance(){
        if (instance == null){
           System.out.println("Making  a new light controller instance");
            instance = new RasppiLightController();
        }

        return instance;
    }

    public void switchOnGreen(){
        greenLightPin.setState(PIN_STATE_ON); // switch off red
        System.out.println("Green ON");
    }

    public void switchOffGreen(){
        greenLightPin.setState(PIN_STATE_OFF); // switch off red
        System.out.println("Green Off");
    }

    public void switchOnRed(){
        redLightPin.setState(PIN_STATE_ON); // switch on red
        System.out.println("Red ON");
    }

    public void switchOffRed(){
        redLightPin.setState(PIN_STATE_OFF); // switch on red
        System.out.println("Red Off");
    }

    public void startBlinking(){
        if (!blinking){
            blinking = true;
            redLightPin.blink(1000);
            greenLightPin.blink(1000);
        }
    }

    public void switchOnBoth(){
        greenLightPin.setState(PIN_STATE_ON); // off green
        redLightPin.setState(PIN_STATE_ON); // switch on red
        System.out.println("Both on");
    }

    public void switchOffBoth(){
//        redLightPin.clearProperties();
//        greenLightPin.clearProperties();

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

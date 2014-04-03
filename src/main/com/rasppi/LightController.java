package com.rasppi;

/**
 * Created with IntelliJ IDEA.
 * User: jerome
 * Date: 3/04/2014
 * Time: 6:57 PM
 * To change this template use File | Settings | File Templates.
 */
public interface LightController {
    void startBlinking();

    void stopBlinking();

    void switchOnGreen();

    void switchOnRed();
}

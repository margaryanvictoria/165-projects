package myGameEngine;

import m1.MyGame; //import the game

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

public class MoveLRAction extends AbstractInputAction {
    private SceneNode avN;

    public MoveLRAction(SceneNode sn) {
        avN = sn;
    }

    public void performAction(float time, Event e) {
        float value = e.getValue();

        if(value > .1f || value < -.1f) {
            avN.moveRight(value*-0.1f);
        }
    }
}
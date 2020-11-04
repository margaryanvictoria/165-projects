package myGameEngine;

import m1.MyGame; //import the game

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

public class MoveBackwardAction extends AbstractInputAction {
    private SceneNode avN;

    public MoveBackwardAction(SceneNode sn) {
        avN = sn;
    }

    public void performAction(float time, Event e) {
        avN.moveBackward(0.1f);
    }
}
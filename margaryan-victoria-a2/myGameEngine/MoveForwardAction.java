package myGameEngine;

import a2.MyGame; //import the game

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

public class MoveForwardAction extends AbstractInputAction {
    private SceneNode avN;

    public MoveForwardAction(SceneNode sn) {
        avN = sn;
    }

    public void performAction(float time, Event e) {
        avN.moveForward(0.1f);
    }
}
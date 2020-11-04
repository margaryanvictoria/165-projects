package myGameEngine;

import m1.MyGame; //import the game

import ray.input.action.AbstractInputAction;
import ray.rage.scene.*;
import ray.rage.game.*;
import ray.rml.*;
import net.java.games.input.Event;

public class MoveLeftAction extends AbstractInputAction {
    private SceneNode avN;

    public MoveLeftAction(SceneNode sn) {
        avN = sn;
    }

    public void performAction(float time, Event e) {
        avN.moveLeft(-0.1f);
    }
}